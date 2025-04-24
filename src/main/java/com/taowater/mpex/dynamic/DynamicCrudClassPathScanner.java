package com.taowater.mpex.dynamic;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.taowater.mpex.dynamic.filter.AllFilter;
import com.taowater.taol.core.bo.Tuple;
import com.taowater.taol.core.reflect.ClassUtil;
import com.taowater.taol.core.reflect.TypeUtil;
import com.taowater.ztream.Ztream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.*;


/**
 * 动态 CRUD 类路径扫描仪
 *
 * @author zhu56
 * @see ClassPathMapperScanner
 */
public class DynamicCrudClassPathScanner extends ClassPathBeanDefinitionScanner {
    protected final Log logger = LogFactory.getLog(getClass());
    private final Class<?> mapperClazz = DynamicMapper.class;
    private final Class<?> repositoryClass = DynamicRepository.class;

    static final String FACTORY_BEAN_OBJECT_TYPE = "factoryBeanObjectType";

    public DynamicCrudClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public void registerFilters() {

        addIncludeFilter(
                AllFilter.builder()
                        .annotation(TableName.class)
                        .build()
        );

//        addIncludeFilter(
//                AllFilter.builder()
//                        .annotation(Component.class)
//                        .superClass(BaseCrudController.class)
//                        .build()
//        );

//        addIncludeFilter(
//                AllFilter.builder()
//                        .annotation(Component.class)
//                        .superClass(BaseAdminService.class)
//                        .build()
//        );

//        addIncludeFilter(
//                AllFilter.builder()
//                        .annotation(Component.class)
//                        .superClass(repositoryClass)
//                        .build()
//        );

//        addIncludeFilter(new AssignableTypeFilter(mapperClazz));


        // exclude package-info.java
        addExcludeFilter(
                (metadataReader, metadataReaderFactory) -> {
                    ClassMetadata classMetadata = metadataReader.getClassMetadata();
                    return classMetadata.getClassName().endsWith("package-info");
                });
    }


    /**
     * 扫描
     *
     * @see ClassPathMapperScanner#scan(String...)
     */
    @Override
    public int scan(String... basePackages) {

        int beanCountAtScanStart = getRegistry().getBeanDefinitionCount();

        Set<BeanDefinitionHolder> bds = this.doScan(basePackages);

        Set<Class<?>> entities = Ztream.of(bds)
                .map(e -> {
                    BeanDefinition bd = e.getBeanDefinition();
                    String beanClassName = bd.getBeanClassName();
                    Class<?> clazz = ClassUtil.fromName(beanClassName);
                    getRegistry().removeBeanDefinition(e.getBeanName());
                    return clazz;
                })
                .nonNull()
                .eq(Class::isMemberClass, false)
                .eq(Class::isAnonymousClass, false)
                .eq(Class::isLocalClass, false)
                .toSet(e -> e);
//        List<Type> controllers = find(getRegistry(), BaseCrudController.class, false);
//        List<Type> services = find(getRegistry(), BaseAdminService.class, false);
        List<Type> repositories = find(getRegistry(), repositoryClass, false);
        List<Type> mappers = find(getRegistry(), mapperClazz, true);

        Ztream.of(entities).hash(e -> e, e -> {
            Map<Class<?>, Type> subMap = new HashMap<>();
            subMap.put(mapperClazz, Ztream.of(mappers).eq(m -> TypeUtil.getTypeArgument(m, mapperClazz), e).getFirst());
            subMap.put(repositoryClass, Ztream.of(repositories).eq(m -> TypeUtil.getTypeArgument(m, repositoryClass), e).getFirst());
//            subMap.put(BaseAdminService.class, Ztream.of(services).eq(s -> TypeUtil.getTypeArgument(s, BaseAdminService.class), e).getFirst());
//            subMap.put(BaseCrudController.class, Ztream.of(controllers).eq(c -> TypeUtil.getTypeArgument(c, BaseCrudController.class), e).getFirst());
            return subMap;
        }).forEachKeyValue((k, v) -> {
            if (v.get(mapperClazz) == null) {
                Class<?> mapper = DynamicHelper.buildMapper(k);
                logger.info("动态构建:" + mapper.getName());
                BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(mapper).getBeanDefinition();
                BeanDefinitionHolder holder = new BeanDefinitionHolder(bd, StrUtil.lowerFirst(mapper.getSimpleName()));
                processBeanDefinitions(holder);
            }
            if (v.get(repositoryClass) == null) {
                Class<?> repository = DynamicHelper.buildRepository(k);
                logger.info("动态构建:" + repository.getName());
                BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(repository).getBeanDefinition();
                getRegistry().registerBeanDefinition(StrUtil.lowerFirst(repository.getSimpleName()), bd);
            }
//            if (v.get(BaseAdminService.class) == null) {
//                Class<?> controller = v.get(BaseCrudController.class);
//                if (Objects.nonNull(controller)) {
//                    Class<?> service = DynamicHelper.buildService((Class<?>) controller);
//                    BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(service).getBeanDefinition();
//                    getRegistry().registerBeanDefinition(StrUtil.lowerFirst(service.getSimpleName()), bd);
//                }
//            }
        });

        return (getRegistry().getBeanDefinitionCount() - beanCountAtScanStart);
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isInterface()
                || metadata.isIndependent()
                && (metadata.isConcrete()
                || (metadata.isAbstract() && metadata.hasAnnotatedMethods(Lookup.class.getName())))
                ;
    }

    public List<Type> find(BeanDefinitionRegistry registry, Class<?> clazz, boolean remove) {
        if (registry instanceof ListableBeanFactory) {
            ListableBeanFactory listableBeanFactory = (ListableBeanFactory) registry;
            return Ztream.of(listableBeanFactory.getBeanNamesForType(ResolvableType.forClass(clazz)))
                    .map(e -> {
                        Tuple<ResolvableType, Boolean> tuple = getBeanResolvableType(registry, e);
                        if (remove && !tuple.right) {
                            registry.removeBeanDefinition(e);
                        }
                        return tuple;
                    })
                    .map(t -> t.left)
                    .toList(ResolvableType::getType);
        }
        return null;

    }

    private Tuple<ResolvableType, Boolean> getBeanResolvableType(BeanDefinitionRegistry registry, String beanName) {
        try {
            if (registry.getBeanDefinition(beanName) instanceof AbstractBeanDefinition) {
                BeanDefinition abd = registry.getBeanDefinition(beanName);
                ResolvableType type = abd.getResolvableType();
                boolean isFactoryBean = false;
                if (type != ResolvableType.NONE) {
                    isFactoryBean = FactoryBean.class.isAssignableFrom(Objects.requireNonNull(type.getRawClass()));
                    if (!isFactoryBean) {
                        return Tuple.of(type, false);
                    }
                }
                Class<?> beanClass = ((ListableBeanFactory) registry).getType(beanName);
                if (beanClass != null) {
                    return Tuple.of(ResolvableType.forClass(beanClass), isFactoryBean);
                }
            }
        } catch (Exception e) {
            // 处理异常
        }
        return null;
    }


    /**
     * @param holder
     * @see ClassPathMapperScanner#processBeanDefinitions(Set)
     */
    private void processBeanDefinitions(BeanDefinitionHolder holder) {
        AbstractBeanDefinition definition;
        BeanDefinitionRegistry registry = getRegistry();
        definition = (AbstractBeanDefinition) holder.getBeanDefinition();
        boolean scopedProxy = false;
        if (ScopedProxyFactoryBean.class.getName().equals(definition.getBeanClassName())) {
            definition = (AbstractBeanDefinition) Optional
                    .ofNullable(((RootBeanDefinition) definition).getDecoratedDefinition())
                    .map(BeanDefinitionHolder::getBeanDefinition).orElseThrow(() -> new IllegalStateException(
                            "The target bean definition of scoped proxy bean not found. Root bean definition[" + holder + "]"));
            scopedProxy = true;
        }
        String beanClassName = definition.getBeanClassName();

        definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
        try {
            Class<?> beanClass = Resources.classForName(beanClassName);
            definition.setAttribute(FACTORY_BEAN_OBJECT_TYPE, beanClass);
            definition.getPropertyValues().add("mapperInterface", beanClass);
        } catch (ClassNotFoundException ignore) {
        }

        definition.setBeanClass(MapperFactoryBean.class);

        definition.getPropertyValues().add("addToConfig", "true");

        boolean explicitFactoryUsed = false;
        String sqlSessionFactoryBeanName = (String) ReflectUtil.getFieldValue(this, "sqlSessionFactoryBeanName");
        SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) ReflectUtil.getFieldValue(this, "sqlSessionFactory");
        if (StringUtils.hasText(sqlSessionFactoryBeanName)) {
            definition.getPropertyValues().add("sqlSessionFactory",
                    new RuntimeBeanReference(sqlSessionFactoryBeanName));
            explicitFactoryUsed = true;
        } else if (sqlSessionFactory != null) {
            definition.getPropertyValues().add("sqlSessionFactory", sqlSessionFactory);
            explicitFactoryUsed = true;
        }
        String sqlSessionTemplateBeanName = (String) ReflectUtil.getFieldValue(this, "sqlSessionTemplateBeanName");
        SqlSessionTemplate sqlSessionTemplate = (SqlSessionTemplate) ReflectUtil.getFieldValue(this, "sqlSessionTemplate");
        if (StringUtils.hasText(sqlSessionTemplateBeanName)) {
            if (explicitFactoryUsed) {
            }
            definition.getPropertyValues().add("sqlSessionTemplate",
                    new RuntimeBeanReference(sqlSessionTemplateBeanName));
            explicitFactoryUsed = true;
        } else if (sqlSessionTemplate != null) {
            if (explicitFactoryUsed) {
            }
            definition.getPropertyValues().add("sqlSessionTemplate", sqlSessionTemplate);
            explicitFactoryUsed = true;
        }

        if (!explicitFactoryUsed) {
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }

        definition.setLazyInit(false);

        if (scopedProxy) {
            return;
        }
        String defaultScope = (String) ReflectUtil.getFieldValue(this, "defaultScope");
        if (ConfigurableBeanFactory.SCOPE_SINGLETON.equals(definition.getScope()) && defaultScope != null) {
            definition.setScope(defaultScope);
        }

        BeanDefinitionHolder finalHolder = holder;
        if (!definition.isSingleton()) {
            finalHolder = ScopedProxyUtils.createScopedProxy(holder, registry, true);
        }
        String beanName = finalHolder.getBeanName();
        if (registry.containsBeanDefinition(beanName)) {
            registry.removeBeanDefinition(beanName);
        }
        registry.registerBeanDefinition(beanName, finalHolder.getBeanDefinition());
    }
}

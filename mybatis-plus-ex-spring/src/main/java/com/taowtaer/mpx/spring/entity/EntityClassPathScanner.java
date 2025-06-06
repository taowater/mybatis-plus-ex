package com.taowtaer.mpx.spring.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.taowater.taol.core.reflect.ClassUtil;
import com.taowater.ztream.Ztream;
import com.taowtaer.mpx.spring.entity.generate.Generator;
import com.taowtaer.mpx.spring.entity.generate.MapperGenerator;
import com.taowtaer.mpx.spring.entity.generate.RepositoryGenerator;
import com.taowtaer.mpx.spring.filter.AllFilter;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 动态类扫描
 *
 * @author zhu56
 */
public class EntityClassPathScanner extends ClassPathScanningCandidateComponentProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    private Class<? extends Generator>[] generators;


    private List<Generator<?>> gs() {
        List<? extends Generator<?>> otherGenerators = Ztream.of(generators).map(e -> {
            Generator<?> generator = ClassUtil.newInstance(e);
            generator.setRegistry(getRegistry());
            return generator;
        }).toList();

        return Ztream.of(
                new MapperGenerator(getRegistry()),
                new RepositoryGenerator(getRegistry())
        ).append(otherGenerators).toList();
    }

    @Nullable
    private ClassLoader classLoader;

    @Getter
    private final BeanDefinitionRegistry registry;

    public EntityClassPathScanner(BeanDefinitionRegistry registry, Class<? extends Generator>[] generators) {
        super(false);
        this.registry = registry;
        if (getRegistry() instanceof DefaultListableBeanFactory) {
            classLoader = ((DefaultListableBeanFactory) registry).getBeanClassLoader();
        } else {
            classLoader = getClass().getClassLoader();
        }
        this.generators = generators;
    }

    public void registerFilters() {

        addIncludeFilter(
                AllFilter.builder()
                        .annotation(TableName.class)
                        .build()
        );

        // exclude package-info.java
        addExcludeFilter(
                (metadataReader, metadataReaderFactory) -> {
                    ClassMetadata classMetadata = metadataReader.getClassMetadata();
                    return classMetadata.getClassName().endsWith("package-info");
                });
    }


    public void scan(String... packageNames) {
        if (logger.isDebugEnabled()) {
            logger.debug("Scanning all types for reflective usage from " + Arrays.toString(packageNames));
        }
        Set<Class<?>> entities = Ztream.of(packageNames)
                .map(this::findCandidateComponents)
                .flat(e -> e)
                .nonNull()
                .toSet(bd -> (Class<?>) bd.getAttribute("type"));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Ztream.of(gs()).forEach(g -> Ztream.of(entities).forEach(g::handle));
        stopWatch.stop();
        BigDecimal scends = BigDecimal.valueOf(stopWatch.getTotalTimeSeconds()).setScale(3, RoundingMode.HALF_UP);
        logger.info("Generated " + 0 + " Classes in " + scends + " seconds");
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        boolean invalid = !metadata.isIndependent()
                || metadata.isInterface()
                || metadata.isAbstract();
        if (invalid) {
            return false;
        }

        String className = beanDefinition.getBeanClassName();
        if (className != null) {
            try {
                Class<?> type = ClassUtils.forName(className, classLoader);
                beanDefinition.setAttribute("type", type);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }
}
package com.taowtaer.mpx.entity;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.taowater.ztream.Ztream;
import com.taowtaer.mpx.entity.generate.Generator;
import com.taowtaer.mpx.entity.generate.MapperGenerator;
import com.taowtaer.mpx.entity.generate.RepositoryGenerator;
import com.taowtaer.mpx.filter.AllFilter;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * 动态类扫描
 *
 * @author zhu56
 */
public class EntityClassPathScanner extends ClassPathScanningCandidateComponentProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    @Nullable
    private final ClassLoader classLoader;

    @Getter
    private final BeanDefinitionRegistry registry;

    public EntityClassPathScanner(@Nullable ClassLoader classLoader, BeanDefinitionRegistry registry) {
        super(false);
        this.classLoader = classLoader;
        this.registry = registry;
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
        long num = handle(entities);
        stopWatch.stop();
        BigDecimal scends = BigDecimal.valueOf(stopWatch.getTotalTimeSeconds()).setScale(3, RoundingMode.HALF_UP);
        logger.info("Generated " + num + " Classes in " + scends + " seconds");
    }

    private long handle(Set<Class<?>> entities) {
        AtomicLong sum = new AtomicLong();

        List<Generator<?>> generators = ListUtil.of(
                new MapperGenerator(getRegistry()),
                new RepositoryGenerator(getRegistry())
        );

        Ztream.of(entities).forEach(e -> {
            generators.forEach(g -> g.handle(e));
        });
        return sum.get();
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
                Class<?> type = ClassUtils.forName(className, this.classLoader);
                beanDefinition.setAttribute("type", type);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }
}
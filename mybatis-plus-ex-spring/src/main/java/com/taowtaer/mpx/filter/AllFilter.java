package com.taowtaer.mpx.filter;

import com.taowater.taol.core.util.EmptyUtil;
import com.taowater.ztream.Ztream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * 组合过滤器
 *
 * @author zhu56
 */
@SuppressWarnings("unused")
public class AllFilter implements TypeFilter {

    private final TypeFilter[] filters;

    public AllFilter(TypeFilter... filters) {
        this.filters = filters;
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
        return Ztream.of(filters).allMatch(e -> {
            try {
                return e.match(metadataReader, metadataReaderFactory);
            } catch (IOException ex) {
                return false;
            }
        });
    }

    public static Builder builder() {
        return new Builder();
    }

    @Accessors(chain = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private Class<? extends Annotation>[] annotations;
        private Class<?>[] superClasses;

        @SafeVarargs
        public final Builder annotation(Class<? extends Annotation>... annotations) {
            this.annotations = annotations;
            return this;
        }

        public final Builder superClass(Class<?>... superClasses) {
            this.superClasses = superClasses;
            return this;
        }

        public AllFilter build() {
            List<TypeFilter> list = Ztream.of(annotations)
                    .map(e -> new AnnotationTypeFilter(e))
                    .cast(TypeFilter.class)
                    .append(Ztream.of(superClasses).map(AssignableTypeFilter::new).cast(TypeFilter.class)).toList();
            if (EmptyUtil.isEmpty(list)) {
                return null;
            }
            return new AllFilter(list.toArray(new TypeFilter[0]));
        }
    }
}

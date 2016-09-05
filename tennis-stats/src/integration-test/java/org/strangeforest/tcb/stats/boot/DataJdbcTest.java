package org.strangeforest.tcb.stats.boot;

import java.lang.annotation.*;

import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.test.autoconfigure.*;
import org.springframework.boot.test.autoconfigure.core.*;
import org.springframework.boot.test.autoconfigure.filter.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.test.context.*;
import org.springframework.transaction.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited

@BootstrapWith(SpringBootTestContextBootstrapper.class)
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(DataJdbcTypeExcludeFilter.class)
@Transactional
@AutoConfigureCache
@AutoconfigureDataJdbc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration
public @interface DataJdbcTest {
	boolean useDefaultFilters() default true;
	ComponentScan.Filter[] includeFilters() default {};
	ComponentScan.Filter[] excludeFilters() default {};
}

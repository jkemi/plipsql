plipsql - Java JDBC helpers
===========================

Helpers for working with plain JDBC from within Java in
a convenient and safe fashion.

Provides `try-with-resource` compatible wrapping of JDBC interfaces,
as well as named parameters and transaction helper.

Compatibility
-------------
 Java 1.7+

Install
-------
with gradle (in `build.gradle`)

    repositories {
      maven { url 'http://maven.plip.org/releases' }  // for release versions
      maven { url 'http://maven.plip.org/snapshots' } // for snapshots
    }
    
    dependencies {
      compile 'org.plip:plipsql:1.0+'
    }


Changes
-------
 * `1.1-SNAPSHOT`
   - Integerated/polished `ParameterStatement` and `SQLParameter` utils.
   - Backwards compatible with `plipsql-1.0.x`.
   - Deprecated `SQLStack(SQLClosable c)` constructor.

 * `1.0.1` _2014-12-10_.
   - Removed erroneus jms api compile dependency.
   - Removed debug logging.

 * `1.0` _2014-09-01_
   - Initial public release.

 * `0.x` _<2014_
   - Private internal usage.

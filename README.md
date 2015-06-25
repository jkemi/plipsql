plipsql - Java JDBC helpers
===========================

Helpers for working with plain JDBC from within Java in
a convenient and safe fashion.

Provides try-with-resource compatible wrapping of JDBC interfaces,
as well as named parameters and transaction helper.

Compatibility
-------------
 Java 1.7+

Changes
-------
 * 1.1-SNAPSHOT
    Integerated/polished ParameterStatement and SQLParameter utils.
    Backwards compatible with plipsql-1.0.x.
    Deprecated SQLStack(SQLClosable c) constructor.

 * 1.0.1 (2014-12-10).
    Removed erroneus jms api compile dependency.
    Removed debug logging.

 * 1.0 (2014-09-01)
    Initial public release.

 * 0.x (<2014)
    Private internal usage.

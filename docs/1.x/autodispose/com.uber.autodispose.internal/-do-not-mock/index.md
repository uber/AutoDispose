[autodispose](../../index.md) / [com.uber.autodispose.internal](../index.md) / [DoNotMock](./index.md)

# DoNotMock

`@Inherited @Target([AnnotationTarget.CLASS, AnnotationTarget.FILE]) class DoNotMock`

This annotation indicates that a given type should not be mocked. This is a copy of what was in Error-Prone's annotations artifact before it was removed, but left for documentation purposes.

This has been modified to have CLASS retention and is only applicable to TYPE targets.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `DoNotMock(value: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`)`<br>This annotation indicates that a given type should not be mocked. This is a copy of what was in Error-Prone's annotations artifact before it was removed, but left for documentation purposes.  |

### Properties

| Name | Summary |
|---|---|
| [value](value.md) | `val value: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>The reason why the annotated type should not be mocked.  |

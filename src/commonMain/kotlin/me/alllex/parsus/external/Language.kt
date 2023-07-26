@file:Suppress("PackageDirectoryMismatch", "unused")

package org.intellij.lang.annotations

/**
 * IntelliJ will inject languages into Strings when it sees this annotation.
 * It's copy-pasted from https://github.com/JetBrains/java-annotations because the `@Language`
 * annotation is JVM only. However, IntelliJ will still recognise it as long as the FQN matches.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.ANNOTATION_CLASS,
)
internal annotation class Language(
    val value: String,
    val prefix: String = "",
    val suffix: String = "",
)

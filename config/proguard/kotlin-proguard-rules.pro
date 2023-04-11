# Proguard rules to workaround Kotlin-specific issues that come up with Proguard.

# These dependencies are actually wrong: the AndroidX versions should be available but current
# Kotlin dependencies seem to reference the support library ones, instead. This could potentially
# run into runtime issues if something is unintentionally removed.
-dontwarn android.support.annotation.Keep
-dontwarn android.support.annotation.VisibleForTesting

# https://github.com/Kotlin/kotlinx.coroutines/issues/2046 describes some of the classes which are
# safe to ignore due to kotlinx.coroutines dependencies. All of the following are sourced from:
# https://github.com/Kotlin/kotlinx.coroutines/blob/bc120a/kotlinx-coroutines-core/jvm/resources/META-INF/com.android.tools/proguard/coroutines.pro
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn java.lang.ClassValue
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn java.lang.instrument.Instrumentation
-dontwarn sun.misc.SignalHandler
-dontwarn sun.misc.Signal
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# The entirety of ClassValueCtorCache is ignored for warnings because it tries to reference an
# inherited method from java.lang.ClassValue (which isn't available in the Android SDK). It will
# eventually be an option per https://issuetracker.google.com/issues/196063118, though only on U+
# devices. This is safe to ignore because kotlinx.coroutines avoids using ExceptionConstructors.kt
# (which houses ClassValueCtorCache & the cache it's referencing) when on Android, per:
# https://github.com/Kotlin/kotlinx.coroutines/pull/2997, specifically:
# https://github.com/Kotlin/kotlinx.coroutines/blob/3574c2/kotlinx-coroutines-core/jvm/src/internal/ExceptionsConstructor.kt#L17.
-dontwarn kotlinx.coroutines.internal.ClassValueCtorCache

# This is an odd issue that seems to come up from buildSequence actually being included in the build
# (since it wasn't removed until 1.7.20 per https://github.com/JetBrains/kotlin/commit/b35c57 but
# was deprecated in https://github.com/JetBrains/kotlin/commit/aac96c. The noteworthy bit here is
# that buildSequence{} was marked as inline-only, so it makes sense that it doesn't exist. This
# should be safe to ignore, though it's a bit unexpected that it's even coming up. It probably can
# go away after upgrading to Kotlin 1.7.x or 1.8.x.
-dontwarn kotlin.sequences.SequencesKt__SequenceBuilderKt$buildSequence*

# BouncyCastle is an optional backend for cryptography-jdk's BouncyCastleBridge.
# We don't ship it, so ProGuard can't resolve these references. They are only
# used reflectively when BouncyCastle is present on the classpath at runtime.
-dontwarn org.bouncycastle.**

//版本号管理
object Versions {
    const val compileSdkVersion = 30
    const val minSdkVersion = 23
    const val targetSdkVersion = 30
    const val lifecycle_version = "2.3.1"
    const val fragment_ktx_version = "1.3.6"
    const val picasso_version = "2.71828"
    const val retrofit_version = "2.9.0"
    const val logging_interceptor_version = "4.7.2"
    const val coroutines_version = "1.4.1"
    const val arouter_api_version = "1.5.0"
    const val arouter_compiler_version = "1.2.2"
}

//三方库管理
object Libs {
    const val livedata_ktx =
        "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle_version}"
    const val viewmodel_ktx =
        "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle_version}"
    const val viewmodel =
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifecycle_version}"
    const val fragment_ktx = "androidx.fragment:fragment-ktx:${Versions.fragment_ktx_version}"
    const val picasso = "com.squareup.picasso:${Versions.picasso_version}"
    const val retofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit_version}"
    const val converter_gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit_version}"
    const val logging_interceptor =
        "com.squareup.okhttp3:logging-interceptor:${Versions.logging_interceptor_version}"
    const val coroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_version}"
    const val coroutines_android =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines_version}"
    const val arouter_api = "com.alibaba:arouter-api:${Versions.arouter_api_version}"
    const val arouter_compiler = "com.alibaba:arouter-compiler:${Versions.arouter_compiler_version}"

    const val CORE_KTX = "androidx.core:core-ktx:1.7.0"
    const val APPCOMPAT = "androidx.appcompat:appcompat:1.3.0"
    const val MATERIAL = "com.google.android.material:material:1.4.0"
    const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:2.0.4"
    const val JUNIT = "junit:junit:4.13.2"
    const val ANDROIDX_JUNIT = "androidx.test.ext:junit:1.1.3"
    const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:3.4.0"

    const val AROUTERS_API = "com.github.jadepeakpoet.ARouter:arouter-api:1.5.0"
    //com.alibaba:arouter-api:1.5.2
    const val AROUTER_COMPILER = "com.github.jadepeakpoet.ARouter:arouter-compiler:1.2.2"
    //com.alibaba:arouter-compiler:1.5.2




    const val PERSISTENT_COOKIE = "com.github.franmontiel:PersistentCookieJar:v1.0.1"

    const val GLIDE = "com.github.bumptech.glide:glide:4.14.2"
    const val GLIDE_COMPILER = "com.github.bumptech.glide:compiler:4.14.2"
    const val PICASSO = "com.squareup.picasso:picasso:2.8"

    const val YOUTH_BANNER = "io.github.youth5201314:banner:2.2.2"

    const val RETROFIT2 = "com.squareup.retrofit2:retrofit:2.9.0"
    const val RETROFIT_RXJAVA2 = "com.squareup.retrofit2:adapter-rxjava2:2.9.0"
    const val RETROFIT_GSON = "com.squareup.retrofit2:converter-gson:2.9.0"
    const val RETROFIT_SCALARS = "com.squareup.retrofit2:converter-scalars:2.9.0"

    const val OKIO = "com.squareup.okio:okio:2.10.0"

    const val RX_ANDROID = "io.reactivex.rxjava2:rxandroid:2.1.0"
    const val RXJAVA2 = "io.reactivex.rxjava2:rxjava:2.2.9"

    const val OKHTTP3 = "com.squareup.okhttp3:okhttp:5.0.0-alpha.2"
    const val OKHTTP_LOGGER = "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2"

    const val LIFECYCLE_LIVEDATA_KTX = "androidx.lifecycle:lifecycle-livedata-ktx:2.5.0"
    const val LIFECYCLE_VIEWMODEL_KTX = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0"
    const val LIFECYCLE_COMPILER = "androidx.lifecycle:lifecycle-compiler:2.5.0"

    const val GOOGLE_GSON = "com.google.code.gson:gson:2.8.6"

    const val  eventbus = "org.greenrobot:eventbus:3.3.1"
}
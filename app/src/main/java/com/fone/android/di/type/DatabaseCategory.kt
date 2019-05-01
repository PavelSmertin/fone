package com.fone.android.di.type

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DatabaseCategory(val value: DatabaseCategoryEnum)

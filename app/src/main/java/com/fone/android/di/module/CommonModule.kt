package one.mixin.android.di.module

import com.fone.android.ui.common.UserBottomSheetDialogFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class CommonModule {
    @ContributesAndroidInjector
    internal abstract fun contributeUserBottomSheetFragment(): UserBottomSheetDialogFragment

}
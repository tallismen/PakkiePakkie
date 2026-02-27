package nl.designlama.pakkiepakkie.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module

fun commonModule() = CommonModule().module

@ComponentScan("nl.designlama.pakkiepakkie")
@Module(includes = [PlatformModule::class])
class CommonModule

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PlatformModule() {
    val module: org.koin.core.module.Module
}

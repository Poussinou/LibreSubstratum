package com.jereksel.libresubstratum.dagger.modules

import android.app.Application
import com.jereksel.libresubstratum.activities.detailed.DetailedPresenter
import com.jereksel.libresubstratum.activities.main.MainPresenter
import com.jereksel.libresubstratum.activities.main.MainContract
import com.jereksel.libresubstratum.activities.detailed.DetailedContract
import com.jereksel.libresubstratum.activities.installed.InstalledContract
import com.jereksel.libresubstratum.activities.installed.InstalledPresenter
import com.jereksel.libresubstratum.domain.*
import com.jereksel.libresubstratum.domain.db.themeinfo.guavacache.ThemeInfoGuavaCache
import com.jereksel.libresubstratum.domain.db.themeinfo.room.RoomThemePackDatabase
import com.jereksel.libresubstratum.domain.usecases.CompileThemeUseCase
import com.jereksel.libresubstratum.domain.usecases.GetThemeInfoUseCase
import com.jereksel.libresubstratum.domain.usecases.ICompileThemeUseCase
import com.jereksel.libresubstratum.domain.usecases.IGetThemeInfoUseCase
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
open class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun providesApplication() = application

    @Provides
    @Singleton
    open fun providesPackageManager(application: Application): IPackageManager {
        return AppPackageManager(application)
    }

    @Provides
    @Singleton
    open fun providesThemeReader(
            packageManager: IPackageManager,
            keyFinder: IKeyFinder
    ): IThemeReader {
        return ThemeReader(application, packageManager, keyFinder)
    }

    @Provides
    @Singleton
    @Named("default")
    open fun providesOverlayService(
            metrics: Metrics
    ): OverlayService {
        val service = OverlayServiceFactory.getOverlayService(application)
        metrics.logOverlayServiceType(service)
        return service
    }

    @Provides
    @Singleton
    @Named("logged")
    open fun providesLoggedOverlayService(
            @Named("default") overlayService: OverlayService,
            metrics: Metrics
    ): OverlayService {
        return LoggedOverlayService(overlayService, metrics)
    }

    @Provides
    @Singleton
    open fun providesActivityProxy(): IActivityProxy = ActivityProxy(application)

    @Provides
    @Singleton
    open fun providesThemeCompiler(
            packageManager: IPackageManager,
            keyFinder: IKeyFinder
    ): ThemeCompiler = AppThemeCompiler(application, packageManager, keyFinder)

    @Provides
    open fun providesMainPresenter(
            packageManager: IPackageManager,
            themeReader: IThemeReader,
            @Named("logged") overlayService: OverlayService,
            metrics: Metrics,
            keyFinder: IKeyFinder
    ): MainContract.Presenter {
        return MainPresenter(packageManager, themeReader, overlayService, metrics, keyFinder)
    }

    @Provides
    @Singleton
    open fun provideThemeExtractor(): ThemeExtractor = BaseThemeExtractor()

    @Provides
    open fun providesDetailedPresenter(
            packageManager: IPackageManager,
            getThemeInfoUseCase: IGetThemeInfoUseCase,
            @Named("logged") overlayService: OverlayService,
            activityProxy: IActivityProxy,
            compileThemeUseCase: ICompileThemeUseCase,
            clipboardManager: ClipboardManager,
            metrics: Metrics
    ): DetailedContract.Presenter {
        return DetailedPresenter(packageManager, getThemeInfoUseCase, overlayService, activityProxy, compileThemeUseCase, clipboardManager, metrics)
    }

    @Provides
    open fun providesInstalledPresenter(
            packageManager: IPackageManager,
            @Named("logged") overlayService: OverlayService,
            activityProxy: IActivityProxy,
            metrics: Metrics
    ): InstalledContract.Presenter {
        return InstalledPresenter(packageManager, overlayService, activityProxy, metrics)
    }

    @Provides
    @Singleton
    open fun providesCompileThemeUseCase(
            packageManager: IPackageManager,
            themeCompiler: ThemeCompiler
    ): ICompileThemeUseCase {
        return CompileThemeUseCase(packageManager, themeCompiler)
    }

    @Provides
    @Singleton
    open fun providesClipBoardManager(): ClipboardManager = AndroidClipboardManager(application)

    @Provides
    @Singleton
    open fun providesKeyFinder(
            packageManager: IPackageManager
    ): IKeyFinder = KeyFinder(application, packageManager)

    @Provides
    @Singleton
    open fun providesThemePackDatabase(
    ): ThemePackDatabase = ThemeInfoGuavaCache()

    @Provides
    @Singleton
    open fun providesGetThemeInfoUseCase(
            packageManager: IPackageManager,
            themePackDatabase: ThemePackDatabase,
            themeReader: IThemeReader
    ): IGetThemeInfoUseCase = GetThemeInfoUseCase(packageManager, themePackDatabase, themeReader)

}

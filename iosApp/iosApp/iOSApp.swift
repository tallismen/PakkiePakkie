import UIKit
import ComposeApp

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

    let envHelper: EnvironmentHelper = EnvironmentHelperImpl()

    lazy var window: UIWindow? = {
        let window = UIWindow(frame: UIScreen.main.bounds)
        let rootViewController = MainKt.MainViewController()
        rootViewController.view.backgroundColor = UIColor.clear
        rootViewController.edgesForExtendedLayout = .all
        rootViewController.extendedLayoutIncludesOpaqueBars = true
        window.rootViewController = rootViewController
        return window
    }()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        Koin_iosKt.doInitKoin(
            appConfig: AppConfig(
                environment: Constants.environment,
                debug: Constants.isDebugMode
            )
        )
        window?.makeKeyAndVisible()
        return true
    }
}

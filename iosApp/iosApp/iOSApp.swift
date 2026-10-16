import SwiftUI
import LittleCloudKit

@main
struct iOSApp: App {
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea()
                .onChange(of: scenePhase) { phase in
                    // Keep Today on the right date when the app comes back after midnight.
                    if phase == .active { MainViewControllerKt.refreshDay() }
                }
        }
    }
}

/// Hosts the shared Compose UI (app/src/commonMain).
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // LC_OPEN lets a test or a shortcut open a given screen ("tab:calendar", "settings" …).
        MainViewControllerKt.MainViewController(open: ProcessInfo.processInfo.environment["LC_OPEN"])
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

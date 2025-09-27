// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorApkUpdater",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorApkUpdater",
            targets: ["CapacitorApkUpdaterPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "CapacitorApkUpdaterPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/CapacitorApkUpdaterPlugin"),
        .testTarget(
            name: "CapacitorApkUpdaterPluginTests",
            dependencies: ["CapacitorApkUpdaterPlugin"],
            path: "ios/Tests/CapacitorApkUpdaterPluginTests")
    ]
)
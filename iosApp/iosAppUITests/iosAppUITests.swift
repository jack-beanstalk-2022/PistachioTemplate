//
//  iosAppUITests.swift
//  iosAppUITests
//
//  Copyright © 2026 orgName. All rights reserved.
//

import XCTest

final class iosAppUITests: XCTestCase {

    private let app = XCUIApplication()

    override func setUpWithError() throws {
        // In UI tests it is usually best to stop immediately when a failure occurs.
        continueAfterFailure = false

        // Set device orientation to portrait before launching
        XCUIDevice.shared.orientation = .portrait
        
        // UI tests must launch the application that they test.
        app.launch()
    }

    override func tearDownWithError() throws {
    }

    @MainActor
    func testScrollingDownGesture() throws {
        // XCUITest has better timing control, we don't need to wait for the app to load at this point

        // Perform swipe up gesture to simulate scrolling down
        // In XCTest, swipeUp() on the app or the main window works.
        app.swipeUp()

        // Wait a bit for the scroll to complete
        Thread.sleep(forTimeInterval: 1.0)
    }
}

//
//  Constants.swift
//  iosApp
//
//  Created by maarten.lammers  on 30/07/2025.
//


import Foundation
import ComposeApp

/// Compile-time constants using Active Compilation Conditions.
/// See Build Settings for each target for configuration.
enum Constants {
    
#if ENV_DEVELOP
    static let environment = AppConfig.Environment.develop
#elseif ENV_STAGING
    static let environment = AppConfig.Environment.staging
#else
    static let environment = AppConfig.Environment.production
#endif
    
#if DEBUG
    static let isDebugMode = true
#else
    static let isDebugMode = false
#endif
    
}

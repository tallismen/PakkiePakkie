//
//  EnvironmentHelper.swift
//  iosApp
//
//  Created by guus.mulder  on 29/07/2025.
//
import Foundation

enum Environment: String {
    case development = "dev"
    case staging = "stag"
    case production = "prod"
}

protocol EnvironmentHelper {
    func getEnvironment() -> Environment
}

class EnvironmentHelperImpl: EnvironmentHelper {

    private let buildSettingsData = Bundle.main.infoDictionary
    
    func getEnvironment() -> Environment {
        let environmentString = buildSettingsData?["ENVIRONMENT_IDENTIFIER"] as? String
        guard let environmentString else {
            print("Missing environment identifier in build settings")
            return .development
        }
        guard let environment = Environment(rawValue: environmentString) else {
            print("Missing environment: \(environmentString)")
            return .development
        }
        return environment
    }
}

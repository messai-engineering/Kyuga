//
//  KyugaTestTests.swift
//  KyugaTestTests
//
//  Created by Vishnu Satis on 21/02/20.
//  Copyright © 2020 Vishnu Satis. All rights reserved.
//

import XCTest
import KyugaIOS

@testable import KyugaTest

class KyugaTestTests: XCTestCase {

    override func setUp() {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDown() {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testExample() {
        // This is an example of a functional test case.
        // Use XCTAssert and related functions to verify your tests produce the correct results.
//        print(formatDatetoDefault(inputDate: NSDate()))
//        print(formatDateToEnglishLocale(inputDate: formatDatetoDefault(inputDate: NSDate())))
        let yuga = Kyuga.init()
        let response: Response = yuga.parse(str: "09:40 PM May 21, 2017")!
        print("----------------------------------->>>")
        print(response.dateStr!)
        print("----------------------------------->>>")
    }
    

    func testPerformanceExample() {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
            readJsonAndValidateYuga()
        }
    }
    
    func formatDatetoDefault(inputDate: NSDate) -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        return dateFormatter.string(from: inputDate as Date)
    }
    
    func formatDateToEnglishLocale(inputDate: String) -> NSDate {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        dateFormatter.locale = NSLocale(localeIdentifier: "en_US") as Locale
        return dateFormatter.date(from: inputDate)! as NSDate
    }
    
    func readJsonAndValidateYuga() {
        let yuga = Kyuga.init()
        let pathString = Bundle(for: type(of: self))
            .path(forResource: "yuga_test_data", ofType: "json");
        print(pathString!)
        let json = try? String(contentsOfFile: pathString!, encoding: .utf8)
        print("The JSON string is: \(json!)")
        let jsonData = json!.data(using: .utf8)
        let jsonDictionary = try? JSONSerialization.jsonObject(with: jsonData!, options: []) as? [String:Any]
        let items : [NSDictionary] = jsonDictionary!["items"] as! [NSDictionary]
        let config = NSMutableDictionary()
        config["YUGA_CONF_DATE"] = "2018-01-01 00:00:00"
        for item in items {
            let input = item["input"] as! String
            print("Working on input: " + input)
            let expectedStr = (item["response"] as! NSDictionary)["str"]! as! String
            let response: Response = yuga.parse(str: input, config: config as! [String : String])!
            XCTAssertEqual(expectedStr, response.dateStr)
        }
    }

}

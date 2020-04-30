# Kyuga
This project is the kotlin multi-platform form of [Yuga](https://github.com/messai-engineering/Yuga).
The library parses text for any date format and returns the date object.
 No need to know format type.

## Getting Started

Its the simplest API, just pass the text, we will return the Date object.

### Prerequisites

None :relaxed:


### Usage

```
val response: Response = KYuga.parse("Friday, 22nd September 4:30 PM");
val date: Date = response.getDate();
```

## Built With

* [kotlin](https://kotlinlang.org/)

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/johnjoseph/b6aeea8ff859964ac325896bf9eeb2c7) for details on our code of conduct, and the process for submitting pull requests to us.

## Latest Releases

* 0.0.5
    * common
      
       [ ![Download](https://api.bintray.com/packages/messai/maven/kyuga/images/download.svg) ](https://bintray.com/messai/maven/kyuga/_latestVersion)
      ```groovy
      implementation "in.messai.kyuga:kyuga:0.0.5"
      ```
    * android
    
       [ ![Download](https://api.bintray.com/packages/messai/maven/kyuga/images/download.svg) ](https://bintray.com/messai/maven/kyuga/_latestVersion)
      ```groovy
      implementation "in.messai.kyuga:kyuga-android:0.0.5"
      ```
    * iOS
        
       [ ![Download](https://api.bintray.com/packages/messai/maven/kyuga/images/download.svg) ](https://bintray.com/messai/maven/kyuga/_latestVersion)
      ```groovy
      implementation "in.messai.kyuga:kyuga-ios:0.0.5"
      ```
## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags).

## Authors

Kotlin contributor:
* [Vishnu Satis](https://github.com/vizsatiz)

Yuga creators:
* [John Joseph](https://github.com/johnjoseph)
* [Glen Martin](https://github.com/glenkalarikkal)

See also the list of [contributors](https://github.com/orgs/messai-engineering/people) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

This project owes thanks to few good people. You know who you are.
We love you.
# License annotations

A simple annotation library to specify licenses in source code, which also show up in the compiled jar.

## How to use

Please replace LICENSE with the license you want to use. Please see [licenses](#licenses) to see all possible licenses
and how to specify them.

Gradle:

```groovy
dependencies {
    implementation 'nl.abelkrijgtalles:license-annotations:LICENSE:main-SNAPSHOT'
}
```

Maven:

```xml

<dependencies>
    <dependency>
        <groupId>nl.abelkrijgtalles.license-annotations</groupId>
        <artifactId>LICENSE</artifactId>
        <version>main-SNAPSHOT</version>
    </dependency>
</dependencies>
```

After that, just use the name of your license, transformed with some rules. These are those rules:

- Remove anything that's between brackets
- Remove anything that isn't a letter or a number
- Use camelcase for every word (An abbreviation should be treated as one word)
- If it doesn't start with a letter, place an underscore before it.

Examples:

- `13/14 Year old license version 1.34 (it's very cool)` will turn into `_1314YearOldLicenseVersion134`.
- `GNU General Public License version 3` will turn into `GnuGeneralPublicLicenseVersion3`.

## Licenses

All the licenses in this library are all [OSI Approved Licenses](https://opensource.org/licenses) with a SPDX ID.

However, you can't use the raw ID. You have to transform it. Here are the rules for that:

- Make everything lowercase
- Replace anything that isn't a letter or a number with an underscore
- If it doesn't start with a letter, place `pkg_` before it.

Examples:

- `13/14 YOL 1.34` will turn into `pkg_13_14_yol_1_34`.
- `GPL-3.0-only` will turn into `gpl_3_0_only`.

If you also want the raw license, that's also possible. Just use the transformed id, with `-included` added after it.

## Variables

The annotations have some optional variables you could specify.
> Because they are all optional, they also have the `@PossiblyEmpty` annotation (the variables can be empty). Feel free
> to use it in your own project!

| Variable          | Type    | Intended use                                                                                             |
|-------------------|---------|----------------------------------------------------------------------------------------------------------|
| project           | string  | The name of the original project.                                                                        |
| projectSourceCode | string  | The source code repository link of the original project.                                                 |
| originalLocation  | string  | The location of the original class.                                                                      |
| firstCopied       | string  | The date when the class was originally copied.                                                           |
| lastUpdate        | string  | When the code was last updated. This could be the original or the edited code, depending on your choice. |
| smallChanges      | boolean | Whether only small changes are being made to the file.                                                   |
| otherInformation  | string  | Additional information.                                                                                  |
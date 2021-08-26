# Dicio assistant

Dicio is a *free and open source* **voice assistant** running on Android. It supports many different **skills** and input/output methods, and it provides both **speech** and **graphical** feedback to a question. It uses [Vosk](https://github.com/alphacep/vosk-api/) for *speech to text*. It has multilanguage support, and is currently available in these languages: English (`en`) and Italian (`it`). Open to contributions :-D

[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/0.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/0.png)
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/1.png)
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/2.png)
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/3.png)
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/4.png)

## Skills

Currently Dicio answers questions about:
- **search**: looks up information on **DuckDuckGo** (and in the future more engines) - _Search for Dicio_
- **weather**: collects weather information from **OpenWeatherMap** - _What's the weather like?_
- **lyrics**: shows **Genius** lyrics for songs - _What's the song that goes we will we will rock you?_
- **open**: opens an app on your device - _Open NewPipe_
- **calculator**: evaluates basic calculations - _What is four thousand and two times three minus a million divided by three hundred?_

## Speech to text

Dicio uses [Vosk](https://github.com/alphacep/vosk-api/) as its speech to text (`STT`) engine. In order to be able to run on every phone small models are employed, weighing `~50MB`. The download from [here](https://alphacephei.com/vosk/models) starts automatically whenever needed, so the app language can be changed seamlessly.

## Contributing

Dicio's code is **not only here**! The repository with the *compiler for sentences* language files is at [`dicio-sentences-compiler`](https://github.com/Stypox/dicio-sentences-compiler), the code taking care of *input matching and skill interfaces* is at [`dicio-skill`](https://github.com/Stypox/dicio-skill) and the *number parser and formatter* is at [`dicio-numbers`](https://github.com/Stypox/dicio-numbers).

When contributing keep in mind that other people may have **needs** and **views different** than yours, so please *respect* them. For any question feel free to contact the project team at [@Stypox](https://github.com/Stypox).

### Translating

If you want to translate Dicio to a new language you have to follow these **steps**:
<ul><li>
  Translate the <b>strings used inside the app</b> via <a href="https://hosted.weblate.org/engage/dicio-android/">Weblate</a>. If your language isn't already there, add it with <a href="https://hosted.weblate.org/new-lang/dicio-android/strings/">tool -> start new translation</a> then add it to the app's language selector (the instructions for this are similar to <a href="https://github.com/TeamNewPipe/NewPipe/wiki/How-to-add-a-new-language-to-NewPipe#add-it-to-the-language-selector">NewPipe's</a>, don't hesitate to ask for help).
  </br>
  <a href="https://hosted.weblate.org/engage/dicio-android/">
  <img src="https://hosted.weblate.org/widgets/dicio-android/-/287x66-grey.png" alt="Translation status" />
  </a>
</li></ul>

- Translate the **sentences** used by Dicio to identify a user's request and to feed it to the correct skill. To do this open the repository root and navigate to `app/src/main/sentences/`. Copy-paste the `en` folder (i.e. the one containing English translations) and call the new folder with the 2- or 3-letter name of your language (in particular, any `ISO-639`-compliant language ID is supported). Then open the newly created folder: inside there should be some files with the `.dslf` extension and in English language. Open each one of them and translate the English content; feel free to add/remove sentences if their translation does not fit into your language and remember those sentences need to identify as better as possible what the user said. Do **NOT** edit the name of the copied files or the first line in them (i.e. the `ID: SPECIFICITY` line, like `weather: high`): they should remain English. To learn about the Dicio sentences language syntax, please refer to the documentation and the [example](https://github.com/Stypox/dicio-sentences-compiler#example) in [`dicio-sentences-compiler`](https://github.com/Stypox/dicio-sentences-compiler#dicio-sentences-language). Hopefully in the future a custom translation system will be used for sentences.

### Adding skills

A skill is a component that enables the assistant to **understand** some specific queries and **act** accordingly. While reading the instructions, keep in mind the skill structure description on the [`dicio-skill` repo](https://github.com/Stypox/dicio-skill/#skills-for-dicio-assistant), the **javadocs** of the methods being implemented and the code of the already implemented skills. In order to add a skill to Dicio you have to follow the steps below, where `SKILL_ID` is the computer readable name of the skill (e.g. `weather`).

#### 1. **Sentences**

Create a file named `SKILL_ID.dslf` (e.g. `weather.dslf`) under `app/src/main/sentences/en/`: it will contain the **sentences** the skill should recognize.
1. Add a *section* to the file by putting `SKILL_ID: SPECIFICITY` (e.g. `weather: high`) on the first line, where `SPECIFICITY` can be `high`, `medium` or `low`. Choose the *specificity* wisely: for example, a section that matches queries about phone calls is very specific, while one that matches every question about famous people has a lower specificity.
2. Fill the rest of the file with *sentences* according to the [`dicio-sentences-language`'s syntax](https://github.com/Stypox/dicio-sentences-compiler#dicio-sentences-language).
3. _\[Optional\]_ If you need to, you can add other sections by adding another `SECTION_NAME: SPECIFICITY` to the same file (check out the calculator skill for why that could be useful). For style reasons, always prefix the section name with `SKILL_ID_` (e.g. `calculator_operators`).
4. _\[Optional\]_ Note that you may choose not to use the standard recognizer; in that case create a class in the skill package overriding [`InputRecognizer`](https://github.com/Stypox/dicio-skill/#input-recognizer). If you do so, replace any reference to `StandardRecognizer` with your recognizer and any reference to `StandardResult` with the result type of your recognizer, while reading the steps below.
5. Try to *build* the app: if it succeeds you did everything right, otherwise you will get errors pointing to syntax errors in the `.dslf` file.

#### 2. **Subpackage**
Create a **subpackage** that will contain all of the classes you are about to add: `org.dicio.dicio_android.skills.SKILLID` (e.g. `org.dicio.dicio_android.skills.weather`).

#### 3. **Output generator**
Create a class named `SKILL_IDOutput` (e.g. `WeatherOutput`): it will contain the code that **talks, displays information or does actions**. It will **not** contain code that fetches data from the internet or does calculations.
1. Create a subclass named `Data` and add to that class some `public` fields representing the *input to the output generator*, i.e. all of the data needed to provide an output.
2. Have the class implement `OutputGenerator<Data>` (e.g. `WeatherOutput implements OutputGenerator<WeatherOutput.Data>`)
3. Override the `generate()` method and implement the output *behaviour* of the skill. In particular, use `SpeechOutputDevice` for speech output and `GraphicalOutputDevice` for graphical output.

#### 4. **Intermediate processor**
Create a class named `PROCESSOR_NAMEProcessor` (e.g. `OpenWeatherMapProcessor`): it will contain the code needed to **turn the recognized data into data ready to be outputted**. Note that the name of the class is not based on the skill id but *on what is actually being done*.
1. Have the class implement `IntermediateProcessor<StandardResult, SKILL_IDOutput.Data>` (e.g. `OpenWeatherMapProcessor implements IntermediateProcessor<StandardResult, WeatherOutput.Data>`). `StandardResult` is the *input* data for the processor, generated by `StandardRecognizer` after having understood a user's sentence; `SKILL_IDOutput.Data`, from [3.2](https://github.com/Stypox/dicio-android#3-output-generator), is the *output* data from the processor to feed to the `OutputGenerator`.
2. Override the `process()` method and put there any code making *network requests or calculations*, then return data ready to be outputted. For example, the weather skill gets the weather information for the city you asked for.
3. _\[Optional\]_ There could be more than one processor for the same skill: you can chain them or use different ones based on some conditions (see [3.3](https://github.com/Stypox/dicio-android#3-output-generator)). The search skill, for example, allows the user to choose the search engine, and has a different processor for each engine.

#### 5. **Skill info**
Create a class named `SKILL_IDInfo` (e.g. `WeatherInfo`) overriding `SkillInfo`: it will contain all of the **information needed to manage your skill**.
1. Create a *constructor* taking no arguments and initialize `super` with the skill id (e.g. `"weather"`), a human readable name, a description, an icon (add Android resources for these last three) and finally whether the skill will have some tunable settings (more on this at point [5.4](https://github.com/Stypox/dicio-android#5-skill-info))
2. Override the `isAvailable()` method and return whether the skill can be used under the *circumstances* the user is in (e.g. check whether the recognizer sentences are translated into the user language with `isSectionAvailable(SECTION_NAME)` (see [1.1](https://github.com/Stypox/dicio-android#1-sentences)) or check whether `context.getNumberParserFormatter() != null`, if your skill uses number parsing and formatting).
3. Override the `build()` method. This is the core method of `SkillInfo`, as it actually *builds* a skill. You shall use `ChainSkill.Builder()` to achieve that: it will create a skill that recognizes input, then passes the recognized input to the intermediate processor(s) which in turn provides the output generator with something to output.
	1. Add `.recognize(new StandardRecognizer(getSection(SectionsGenerated.SECTION_NAME)))` as the first function. `SECTION_NAME` is `SKILL_ID`, if you followed the naming scheme from [1.1](https://github.com/Stypox/dicio-android#1-sentences), e.g. `SectionsGenerated.weather`.
	2. Add `.process(new PROCESSOR_NAMEProcessor())`: add the processor you built at step [4](https://github.com/Stypox/dicio-android#4-intermediate-processor), e.g. `new OpenWeatherMapProcessor()`.
	3. _\[Optional\]_ Implement here any condition on processors: for example, query settings to choose the service the user wants, etc. If you wish, you can chain multiple processors together; just make sure the output/input types of consecutive processors match. For an example of this check out the search skill, that uses the search engine chosen by the user.
	4. At the end add `.output
4. _\[Optional\]_ If your skill wants to present some preferences to the user, it has to do so by overriding `getPreferenceFragment()` (return `null` otherwise). Create a subclass of `SKILL_IDInfo` named `Preferences` extending `PreferenceFragmentCompat` (Android requires you not to use anonymous classes) and override the `onCreatePreferences()` as you would do normally. `getPreferenceFragment()` should then `return new Preferences()`. Make sure the `hasPreferences` parameter you use in the constructor (see [5.1](https://github.com/Stypox/dicio-android#5-skill-info)) reflects whether there are preferences or not. 

#### **Notes**
- `skillContext` is provided in many places and can be used to **access resources and services**, similarly to Andorid's `context`.
- If your input recognizer, processor or output generator use some resources that need to be cleaned up in order **not to create memory leaks**, make sure to override the `cleanup()` method.
- If the skill **doesn't do any processing** (e.g. it may just answer with random quotes from famous people after a request for quotes by the user) you may skip step [4](https://github.com/Stypox/dicio-android#4-intermediate-processor) above. Also skip [3.1](https://github.com/Stypox/dicio-android#3-output-generator) in that case, and have `SKILL_IDOutput` implement `OutputGenerator<StandardResult>`.
- The **names** used for things (files, classes, packages, sections, etc.) are not mandatory, but they help **avoiding confusion**, so try to stick to them.
- When committing changes about a skill, prefix the commit message with "[SKILL_ID]", e.g. "[Weather] Fix crash".
- Add your skill with a short description and an example in the README under [Skills](https://github.com/Stypox/dicio-android#skills) and in the [fastlane's long description](https://github.com/Stypox/dicio-android/tree/master/fastlane/metadata/android/en-US/full_description.txt).
- If you have any question, **don't hesitate** to ask. 😃

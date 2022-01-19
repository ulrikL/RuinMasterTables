# RuinMastersTables

This is an Android application to help roll on all the tables used
in the TTRPG Ruin Masters. It will also assist with details on monsters
that may be encountered.

The tables are described in several `app\src\main\assets\tables\*.json`
files. One for each type of terrain.

The *.json files are designed to be done one for each button to be
pressed in the UI.

The monsters are described in several `app\src\main\assets\monsters\*.json`
files. One for each type of monster.

## File formats

The JSON file format for the tables and the monsters are defined below.

### Table format

Each table is represented with an Id, a Name and a set of Options.

The Ids use an offset to separate terrain, encounter and treasure tables
into different ranges. Each Id must be unique and distinct in each
configuration file. It must be provided.

Name shall match the name of the table as defined in the rulebook. It must
be provided.

Options is a list of possible outcomes from the table. At least one Option
must be provided.

#### Option format

Each Option has a Chance, a Table and a Text.

Chance is added for all of the Options in a table and a random
value in the range **1..[sum of chance]** is used to pick which Option
that will be selected. It must be provided.

Table describe an arraylist of which tables that shall be used after this
result. It may be empty.

Text contain the description from the tables in Ruin Masters. It may be
an empty string.

#### Text string format

The logic will replace **'[\<int>d\<int>]'** in the text strings with random
values according to the number of dice and die type.

### Monster format

Each monster must have an Id that is unique per JSON file, the Id range per
JSON file is **1-9**.

The maximum number of attacks for a monster in the JSON file is set to 6.

The maximum number of abilities for a monster in the JSON file is set to 10.

The supported types of the body types are **humanoid**, **quadruped**,
**giant**, **winged_quadruped**, **snake**, **spirit**, **centaur**,
**winged_humanoid**.

If **worn_armor** is to be used the number of values listed must match
the number of body parts for the used body type.

#### Text string format for randomized traits

Traits that can be randomized are **phy**, **min**, **int**, **cha**. 
The logic will replace **'[\<int>d\<int>+\<int>]'**, **'[\<int>d\<int>-\<int>]'**
and **'[\<int>d\<int>]'** in the text strings with random values according to
the number of dice, die type and modifier.

#### Calculated values

Values that can be randomized or based on randomized values are **hp**,
**car** and **db**. To do this they shall be left empty in the JSON file.

# Releasing

1. In `app/build.gradle` update the following values.
   Major step when doing non-backwards compatible changes or major extensions, e.g. changing json file formats, adding new tabs
   Minor step when doing something smaller, e.g. adding monsters or fixing bugs

>   versionCode 3
>   versionName "3.0"

2. In Android Studio select `Build->Generate Signed Bundle/APK`
3. Select APK
4. Create a folder for the release in `app\release\`
5. Put the generated APK there

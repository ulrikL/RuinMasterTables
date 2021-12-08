#RuinMastersTables

This is an Android application to help roll on all the tables used
in the TTRPG Ruin Masters.

The tables are described in a app\src\main\assets\*.json file.

The *.json files are designed to be done one for each button to be
pressed in the UI.

##Table format

Each table is represented with an Id, a name and a set of Options.
The Ids use an offset to separate terrain, encounter and treasure tables
into different ranges.

Each Option has a Chance, a Table and a Text.

Chance is summarized for all of the Options in a table and a random
value in the range 1..<sum of chance> is used to pick which Option
that will be chosen.

Table describe a list of which tables that shall be used after this
result.

Text contain the description from the tables in Ruin Masters.

##Text string format

The logic will replace '[<int>d<int>]' in the text strings with random
values according to the number of dice and die type.

# RuinMastersTables
This is an Android application to help roll on all the tables used
in the TTRPG Ruin Masters.

The tables are described in a `app\src\main\assets\*.json` file.

The *.json files are designed to be done one for each button to be
pressed in the UI.

## Table format
Each table is represented with an Id, a Name and a set of Options.

The Ids use an offset to separate terrain, encounter and treasure tables
into different ranges. Each Id must be unique and distinct in each
configuration file. It must be provided.

Name shall match the name of the table as defined in the rulebook. It must
be provided.

Options is a list of possible outcomes from the table. At least one Option
must be provided.

## Option format
Each Option has a Chance, a Table and a Text.

Chance is added for all of the Options in a table and a random
value in the range **1..[sum of chance]** is used to pick which Option
that will be selected. It must be provided.

Table describe an arraylist of which tables that shall be used after this
result. It may be empty.

Text contain the description from the tables in Ruin Masters. It may be
an empty string.

## Text string format
The logic will replace **'[\<int>d\<int>]'** in the text strings with random
values according to the number of dice and die type.

Operation Model
===============

The assertions from an IdP are stored in an associative array. A
sequence of rules are applied, the first rule which returns success is
considered a match. During the execution of each rule values from the
assertion can be tested and transformed with the results selectively
stored in variables local to the rule. If the rule succeeds an
associative array of mapped values is returned. The mapped values are
taken from the local variables set during the rule execution. The
definition of the rules and mapped results are expressed in JSON
notation.

A rule is somewhat akin to a function in a programming language. It
starts execution with a set of predefined local variables. It executes
statements which are grouped together in blocks. Execution continues
until an `exit`_ statement returning a success/fail result is
executed or until the last statement is reached which implies
success. The remaining statements in a block may be skipped via a
`continue`_ statement which tests a condition, this is equivalent to
an "if" control flow of logic in a programming language.

Rule execution continues until a rule returns success. Each rule has a
`mapping`_ associative array bound to it which is a template for the
transformed result. Upon success the `mapping`_ template for the
rule is loaded and the local variables from the successful rule are
used to populate the values in the `mapping`_ template yielding the
final mapped result.

If no rules returns success authentication fails.


Pseudo Code Illustrating Operational Model
------------------------------------------

::

    mapped = null
    foreach rule in rules {
        result = null
        initialize rule.variables with pre-defined values

        foreach block in rule.statement_blocks {
            for statement in block.statements {
                if statement.verb is exit {
                    result = exit.status
                    break
                }
                elif statement.verb is continue {
                    break
                }
            }
            if result {
                break
            }
        if result == null {
            result = success
        }
    if result == success {
        mapped = rule.mapping(rule.variables)
    }
    return mapped



Structure Of Rule Definitions
=============================

Rules are loaded by the rule processor via a JSON document called a
rule definition. A definition has an *optional* set of mapping
templates and a list of rules. Each rule has specifies a mapping
template and has a list of statement blocks. Each statement block has
a list of statements.

In pseudo-JSON (JSON does not have comments, the ... ellipsis is a
place holder):

::

    {
        "mappings": {
                        "template1": "{...}",
                        "template2": "{...}"
                    },
        "rules": [
                     {   # Rule 0. A rule has a mapping or a mapping name
                         # and a list of statement blocks

                         "mapping": {...},
                         # -OR-
                         "mapping_name": "template1",

                         "statement_blocks": [
                                                 [   # Block 0
                                                     [statement 0]
                                                     [statement 1]
                                                 ],
                                                 [   # Block 1
                                                     [statement 0]
                                                     [statement 1]
                                                 ],

                                              ]
                     },
                     {   # Rule 1 ...
                     }
                 ]

    }

Mapping
-------

A mapping template is used to produce the final associative array of
name/value pairs. The template is a JSON Object. The value in a
name/value pair can be a constant or a variable. If the template value
is a variable the value of the variable is retrieved from the set of
local variables bound to the rule thereby replacing it in the final
result.

For example given this mapping template and rule variables in JSON:

template:

::

    {
        "organization": "BigCorp.com",
        "user: "$subject",
        "roles": "$roles"
    }

local variables:

::

    {
        "subject": "Sally",
        "roles": ["user", "admin"]
    }

The final mapped results would be:

::

    {
        "organization": "BigCorp.com",
        "user: "Sally",
        "roles": ["user", "admin"]
    }


Each rule must bind a mapping template to the rule. The mapping
template may either be defined directly in the rule via the
``mapping`` key or referenced by name via the ``mapping_name`` key.

If the ``mapping_name`` is specified the mapping is looked up in a
table of mapping templates bound to the Rule Processor. Using the name
of a mapping template is useful when many rules generate the exact
same template values.

If both ``mapping`` and ``mapping_name`` are defined the locally bound
``mapping`` takes precedence.

Syntax
------

The logic for a rule consists of a sequence of statements grouped in
blocks. A statement is similar to a function call in a programming
language.

A statement is a list of values the first of which is a verb which
defines the operation the statement will perform. Think of the
`verbs`_ as function names or operators. Following the verb are
parameters which may be constants or variables. If the statement
assigns a value to a variable left hand side of the assignment (lhs)
is always the first parameter following the verb in the list of
statement values.

For example this statement in JSON:

::

    ["split", "$groups", "$assertion[Groups]", ":"]

will assign an array to the variable ``$groups``. It looks up the
string named ``Groups`` in the assertion which is a colon (:)
separated list of group names splitting that string on the colon
character.

Statements **must** be grouped together in blocks. Therefore a rule is
a sequence of blocks and block is a sequence of statements. The
purpose of blocks is allow for crude flow of control logic. For
example this JSON rule has 4 blocks.

::

    [
        [
            ["set", $user, ""],
            ["set", $roles, []]
        ],
        [
            ["in", "UserName", "$assertion"],
            ["continue", "if_not_success"],
            ["set", "$user", "$assertion[UserName"],
        ],
        [
            ["in", "subject", "$assertion"],
            ["continue", "if_not_success"],
            ["set", "$user", "$assertion[subject]"],
        ],
        [
            ["length", "$temp", "$user"],
            ["compare", "$temp", ">", 0],
            ["exit", "rule_fails", "if_not_success"]
            ["append" "$roles", "unprivileged"]
        ]
    ]

The rule will succeed if either ``UserName`` or ``subject`` is defined
in the assertion and if so the local variable ``$user`` will be set to
the value found in the assertion and the "unprivileged" role will be
appended to the roles array.

The first block performs initialization. The second block tests to see
if the assertion has the key ``UserName`` if not execution continues
at the next block otherwise the value of UserName in the assertion is
copied into the variable ``$user``. The third block performs a similar
operation looking for a ``subject`` in the assertion. The fourth block
checks to see if the ``$user`` variable is empty, if it is empty the
rule fails because it didn't find either a ``UserName`` nor a
``subject`` in the assertion. If ``$user`` is not empty the
"unprivileged" role is appended and the rule succeeds.

Data Types
----------

There are 7 supported types which equate to the types available in
JSON. At the time of this writing there are 2 implementations of this
Mapping specification, one in Python and one in Java. This table
illustrates how each data type is represented. The first two columns
are definitions from an abstract specification. The JSON column
enumerates the data type JSON supports.  The Mapping column lists the
7 enumeration names used by the Mapping implemenation in each
language. The following columns list the concrete data type used in
that language.

+-----------+------------+--------------------+---------------------+
|  JSON     |  Mapping   | Python             |       Java          |
+===========+============+====================+=====================+
|  object   |  MAP       | dict               | Map<String, Object> |
+-----------+------------+--------------------+---------------------+
|  array    |  ARRAY     | list               | List<Object>        |
+-----------+------------+--------------------+---------------------+
|  string   |  STRING    | unicode (Python 2) | String              |
|           |            +--------------------+                     |
|           |            | str (Python 3)     |                     |
+-----------+------------+--------------------+---------------------+
|           |  INTEGER   | int                | Long                |
|  number   +------------+--------------------+---------------------+
|           |  REAL      | float              | Double              |
+-----------+------------+--------------------+---------------------+
|  true     |            |                    |                     |
+-----------+  BOOLEAN   | bool               | Boolean             |
|  false    |            |                    |                     |
+-----------+------------+--------------------+---------------------+
|  null     |  NULL      | None               | null                |
+-----------+------------+--------------------+---------------------+


Rule Debugging and Documentation
--------------------------------

If the rule processor reports an error or if you're debugging your
rules by enabling DEBUG log tracing then you must be able to correlate
the reported statement to where it appears in your rule JSON source. A
message will always identify a statement by the rule number, block
number within that rule and the statement number within that
block. However once your rules become moderately complex it will
become increasingly difficult to identify a statement by counting
rules, blocks and statements.

A better approach is to tag rules and blocks with a name or other
identifying string. You can set the `Reserved Variables`_
``rule_name`` and ``block_name`` to a string of your choice. These
strings will be reported in all messages along with the rule, block
and statement numbers.

JSON does not permit comments, as such you cannot include explanatory
comments next to your rules, blocks and statements in the JSON
source. The ``rule_name`` and ``block_name`` can serve a similar
purpose. By putting assignments to these variables as the first
statement in a block you'll both document your rules and be able to
identify specific statements in log messages.

During rule execution the ``rule_name`` and ``block_name`` are
initialized to the empty string at the beginning of each rule and
block respectively.

The above example is augmented to include this information. The rule
name is set in the first statement in the first block.

::

    [
        [
            ["set", "$rule_name", "Must have UserName or subject"],
            ["set", "block_name", "Initialization"],
            ["set", $user, ""],
            ["set", $roles, []]
        ],
        [
            ["set", "block_name", "Test for UserName, set $user"],
            ["in", "UserName", "$assertion"],
            ["continue", "if_not_success"],
            ["set", "$user", "$assertion[UserName"],
        ],
        [
            ["set", "block_name", "Test for subject, set $user"],
            ["in", "subject", "$assertion"],
            ["continue", "if_not_success"],
            ["set", "$user", "$assertion[subject]"],
        ],
        [
            ["set", "block_name", "If not $user fail, else append unprivileged to roles"],
            ["length", "$temp", "$user"],
            ["compare", "$temp", ">", 0],
            ["exit", "rule_fails", "if_not_success"]
            ["append" "$roles", "unprivileged"]
        ]
    ]




Variables
---------


Variables always begin with a dollar sign ($) and are followed by an
identifier which is any alpha character followed by zero or more
alphanumeric or underscore characters. The variable may optionally be
delimited with braces ({}) to separate the variable from surrounding
text. Three types of variables are supported:

* scalar
* array (indexed by zero based integer)
* associative array (indexed by string)

Both arrays and associative arrays use square brackets ([]) to specify
a member of the array. Examples of variable usage:

::

    $name
    ${name}
    $groups[0]
    ${groups[0]}
    $properties[key]
    ${properties[key]}

An array or an associative array may be referenced by it's base name
(omitting the indexing brackets). For example the associative array
array named "properties" is referenced using it's base name
``$properties`` but if you want to access a member of the "properties"
associative array named "duration" you would do this ``$properties[duration]``

This is not a general purpose language with full expression
syntax. Only one level of variable lookup is supported. Therefore
compound references like this

::

    $properties[$groups[2]]

will not work.


Escaping
^^^^^^^^

If you need to include a dollar sign in a string (where it is
immediately followed by either an identifier or a brace and identifier)
and do not want to have it be interpreted as representing a variable
you must escape the dollar sign with a backslash, for example
"$amount" is interpreted as the variable ``amount`` but "\\$amount"
is interpreted as the string "$amount" .


Reserved Variables
------------------

A rule has the following reserved variables:

assertion
    The current assertion values from the federated IdP. It is a
    dictionary of key/value pairs.

regexp_array
    The regular expression groups from the last successful regexp match
    indexed by number. Group 0 is the entire match. Groups 1..n are
    the corresponding parenthesized group counting from the left. For
    example regexp_array[1] is the first group.

regexp_map
    The regular expression groups from the last successful regexp match
    indexed by group name.

rule_number
    The zero based index of the currently executing rule.

rule_name
    The name of the currently executing rule. If the rule name has not
    been set it will be the empty string.

block_number
    The zero based index of the currently executing block within the
    currently executing rule.

block_name
    The name of the currently executing block. If the block name has not
    been set it will be the empty string.


statement_number
    The zero based index of the currently executing statement within the
    currently executing block.


Examples
========

Split a fully qualified username into user and realm components
---------------------------------------------------------------

It's common for some IdP's to return a fully qualified username
(e.g. principal or subject). The fully qualified username is the
concatenation of the user name, separator and realm name. A common
separator is the @ character. In this example lets say the fully
qualified username is ``bob@example.com`` and you want to return the
user and realm as independent values in your mapped result. The
username appears in the assertion as the value ``Principal``.

Our strategy will be to use a regular expression identify the user and
realm components and then assign them to local variables which will
then populate the mapped result.

The mapping in JSON is:

::

    {
        "user": "$username",
        "realm": "$domain"
    }

The assertion in JSON is:

::

    {
        "Principal": "bob@example.com"
    }

Our rule is:

::

    [
        [
            ["in", "Principal", "assertion"],
            ["exit", "rule_fails", "if_not_success"],
            ["regexp", "$assertion[Principal]", (?P<username>\\w+)@(?P<domain>.+)"],
            ["set", "$username", "$regexp_map[username]"],
            ["set", "$domain", "$regexp_map[domain]"],
            ["exit, "rule_succeeds", "always"]
        ]
    ]

Rule explanation:

Block 0:

0. Test if the assertion contains a Principal value.
1. Abort the rule if the assertion does not contain a Principal
   value.
2. Apply a regular expression the the Principal value. Use named
   groupings for the username and domain components for clarity.
3. Assign the regexp group username to the $username local variable.
4. Assign the regexp group domain to the $domain local variable.
5. Exit the rule, apply the mapping, return the mapped values. Note, an
   explicit `exit`_ is not required if there are no further statements
   in the rule, as is the case here.

The mapped result in JSON is:

::

    {
        "user": "bob",
        "realm": "example.com"
    }

Build a set of roles based on group membership
----------------------------------------------

Often one wants to grant roles to a user based on their membership in
certain groups. In this example let's say the assertion contains a
``Groups`` value which is a colon separated list of group names. Our
strategy is to split the ``Groups`` assertion value into an array of
group names. Then we'll test if a specific group is in the groups
array, if it is we'll add a role. Finally if no roles have been mapped
we fail. Users in the group "student" will get the role "unprivileged"
and users in the group "helpdesk" will get the role "admin".

The mapping in JSON is:

::

    {
        "roles": "$roles",
    }

The assertion in JSON is:

::

    {
        "Groups": "student:helpdesk"
    }

Our rule is:

::

    [
        [
            ["in", "Groups", "assertion"],
            ["exit", "rule_fails", "if_not_success"],
            ["set", "$roles", []],
            ["split", "$groups", "$assertion[Groups]", ":"],
        ],
        [
            ["in", "student", "$groups"],
            ["continue", "if_not_success"],
            ["append", "$roles", "unprivileged"]
        ],
        [
            ["in", "helpdesk", "$groups"],
            ["continue", "if_not_success"],
            ["append", "$roles", "admin"]
        ],
        [
            ["unique", "$roles", "$roles"],
            ["length", "$temp", "roles"],
            ["compare", $temp", ">", 0],
            ["exit", "rule_fails", "if_not_success"]
        ]

    ]

Rule explanation:

Block 0

0. Test if the assertion contains a Groups value.
1. Abort the rule if the assertion does not contain a Groups
   value.
2. Initialize the $roles variable to an empty array.
3. Split the colon separated list of group names into an array of
   individual group names

Block 1

0. Test if "student" is in the $groups array
1. Exit the block if it's not.
2. Append "unprivileged" to the $roles array

Block 2

0. Test if "helpdesk" is in the $groups array
1. Exit the block if it's not.
2. Append "admin" to the $roles array

Block 3

0. Strip any duplicate roles that might have been appended to the
   $roles array to assure each role is unique.
1. Count how many members are in the $roles array, assign the
   length to the $temp variable.
2. Test to see if the $roles array had any members.
3. Fail if no roles had been assigned.

The mapped result in JSON is:

::

    {
        "roles": ["unprivileged", "admin"]
    }

However, suppose whatever is receiving your mapped results is not
expecting an array of roles. Instead it expects a comma separated list
in a string. To accomplish this add the following statement as the
last one in the final block:

::

            ["join", "$roles", "$roles", ","]

Then the mapped result will be:

::

    {
        "roles": "unprivileged,admin"]
    }




White list certain users and grant them specific roles
------------------------------------------------------

Suppose you have certain users you always want to unconditionally
accept and authorize with specific roles. For example if the user is
"head_of_IT" then assign her the "user" and "admin" roles. Otherwise
keep processing. The list of white listed users is hard-coded into the
rule.

The mapping in JSON is:

::

    {
        "user": $user,
        "roles": "$roles",
    }

The assertion in JSON is:

::

    {
        "UserName": "head_of_IT"
    }

Our rule in JSON is:

::

    [
        [
            ["in", "UserName", "assertion"],
            ["exit", "rule_fails", "if_not_success"],
            ["in", "$assertion[UserName]", ["head_of_IT", "head_of_Engineering"]],
            ["continue", "if_not_success"],
            ["set", "$user", "$assertion[UserName"]
            ["set", "$roles", ["user", "admin"]],
            ["exit", "rule_succeeds", "always"]
        ],
        [
            ...
        ]
    ]

Rule explanation:

Block 0

0. Test if the assertion contains a UserName value.
1. Abort the rule if the assertion does not contain a UserName
   value.
2. Test if the user is in the hardcoded list of white listed users.
3. If the user isn't in the white listed array then exit the block and
   continue execution at the next block.
4. Set the $user local variable to $assertion[UserName]
5. Set the $roles local variable to the hardcoded array containing
   "user" and "admin"
6. We're done, unconditionally exit and return the mapped result.

Block 1

0. Further processing

The mapped result in JSON is:

::

    {
        "user": "head_of_IT",
        "roles": ["users", "admin"]
    }


Black list certain users
------------------------

Suppose you have certain users you always want to unconditionally
deny access to by placing them in a black list. In this example the
user "BlackHat" will try to gain access. The black list includes the
users "BlackHat" and "Spook".

The mapping in JSON is:

::

    {
        "user": $user,
        "roles": "$roles",
    }

The assertion in JSON is:

::

    {
        "UserName": "BlackHat"
    }

Our rule in JSON is:

::

    [
        [
            ["in", "UserName", "assertion"],
            ["exit", "rule_fails", "if_not_success"],
            ["in", "$assertion[UserName]", ["BlackHat", "Spook"]],
            ["exit", "rule_fails", "if_success"]
        ],
        [
            ...
        ]
    ]

Rule explanation:

Block 0

0. Test if the assertion contains a UserName value.
1. Abort the rule if the assertion does not contain a UserName
   value.
2. Test if the user is in the hard-coded list of black listed users.
3. If the test succeeds then immediately abort and return failure.

Block 1

0. Further processing

The mapped result in JSON is:

::

    Null

Format Strings and/or Concatenate Strings
-----------------------------------------

You can replace variables in a format string using the `interpolate`_
verb. String concatenation is trivially placing two variables adjacent
to one another in a format string. Suppose you want to form an email
address from the username and domain in an assertion.

The mapping in JSON is:

::

    {
        "email": $email,
    }

The assertion in JSON is:

::

    {
        "UserName": "Bob",
        "Domain": "example.com"
    }

Our rule in JSON is:

::

    [
        [
            ["interpolate", "$email", "$assertion[UserName]@$assertion[Domain]"],
        ]
    ]

Rule explanation:

Block 0

0. Replace the variable $assertion[UserName] with it's value and
   replace the variable $assertion[Domain] with it's value.

The mapped result in JSON is:

::

    {
        "email": "Bob@example.com",
    }


Note, sometimes it's necessary to utilize braces to separate variables
from surrounding text by using the brace notation. This can also make
the format string more readable. Using braces to delimit variables the
above would be:

::

    [
        [
            ["interpolate", "$email", "${assertion[UserName]}@${assertion[Domain]}"],
        ]
    ]



Make associative array lookups case insensitive
-----------------------------------------------

Many systems treat field names as case insensitive. By default
associative array indexing is case sensitive. The solution is to lower
case all the keys in an associative array and then only use lower case
indices. Suppose you want the assertion associative array to be case
insensitive.

The mapping in JSON is:

::

    {
        "user": $user,
    }

The assertion in JSON is:

::

    {
        "UserName": "Bob"
    }

Our rule in JSON is:

::

    [
        [
            ["lower", "$assertion", "$assertion"],
            ["in", "username", "assertion"],
            ["exit", "rule_fails", "if_not_success"],
            ["set", "$user", "$assertion[username"]
        ]
    ]

Rule explanation:

Block 0

0. Lower case all the keys in the assertion associative array.
1. Test if the assertion contains a username value.
2. Abort the rule if the assertion does not contain a username
   value.
3. Assign the username value in the assertion to $user

The mapped result in JSON is:

::

    {
        "user": "Bob",
    }


Verbs
=====

The following verbs are supported:

* `set`_
* `length`_
* `interpolate`_
* `append`_
* `unique`_
* `regexp`_
* `regexp_replace`_
* `split`_
* `join`_
* `lower`_
* `upper`_
* `compare`_
* `in`_
* `not_in`_
* `exit`_
* `continue`_

Some verbs have a side effects. A verb may set a boolean success/fail
result which may then be tested with a subsequent verb. For example
the ``fail`` verb can be used to indicate the rule fails if a prior
result is either ``success`` or ``not_success``.  The ``regexp`` verb
which performs a regular expression search on a string stores the
regular expression sub-matches as a side effect in the variables
``$regexp_array`` and ``$regexp_map``.


Verb Definitions
================

set
---

``set $variable value``

$variable
    The variable being assigned (i.e. lhs)

value
    The value to assign to the variable (i.e. rhs). The value may be
    another variable or a constant.

**set** assigns a value to a variable, in other words it's an
assignment statement.

Examples:
^^^^^^^^^

Initialize a variable to an empty array.

::

    ["set", "$groups", []]

Initialize a variable to an empty associative array.

::

    ["set", "$groups", {}]

Assign a string.

::

    ["set", "$version", "1.2.3"]

Copy the ``UserName`` value from the assertion to a temporary variable.

::

    ["set", "$temp", "$assertion[UserName]"],


Get the 2nd item in an array (array indexing is zero based)

::

    ["set", "$group", "$groups[1]"]


Set the associative array entry "IdP" to "kdc.example.com".

::

    ["set", "$metadata[IdP]", "kdc.example.com""]

--------------------------------------------------------------------------------

length
------

``length $variable value``

$variable
    The variable which receives the length value

value
    The value whose length is to be determined. May be one of array,
    associative array, or string.

**length** computes the number of items in the value. How this is done
depends upon the type of value:

array
    The length is the number of items in the array.

associative array
    The length is the number of key/value pairs in the associative
    array.

string
    The length is the number of *characters* (not octets) in the
    string.

Examples:
^^^^^^^^^

Count how many items are in the ``$groups`` array and assign that
value to the ``$groups_length`` variable.

::

    ["length", "$groups_length", "$groups"]

Count how many key/value pairs are in the ``$assertion`` associative
array and assign that value to the ``$num_assertion_values`` variable.

::

    ["length", "$num_assertion_values", "$assertion"]

Count how many characters are in the assertion's UserName and assign
the value to ``$username_length``.

::

    ["length", "$user_name_length", "$assertion[UserName]"]


--------------------------------------------------------------------------------

interpolate
-----------

``interpolate $variable string``

$variable
    This variable is assigned the result of the interpolation.

string
    A string containing references to variables which will be replaced
    in the string.

**interpolate** replaces each occurrence of a variable in a string with
it's value. The result is assigned to $variable.

Examples:
^^^^^^^^^

Form an email address given the username and domain. If the username
is "jane" and the domain is "example.com" then $email will be
"jane@example.com"

::

    ["interpolate", "$email", "${username}@${domain}"]


--------------------------------------------------------------------------------


append
------

``append $variable value``

$variable
    This variable **must** be an array. It is modified in place by
    appending ``value`` to the end of the array.

value
    The value to append to the end of the array.

**append** adds a value to end of an array.

Examples:
^^^^^^^^^

Append the role "qa_test" to the roles list.

::

    ["append", "$roles", "qa_test"]


--------------------------------------------------------------------------------


unique
------

``unique $variable value``

$variable
    This variable is assigned the unique values in the ``value``
    array.

value
    An array of values. **must** be an array.

**unique** builds an array of unique values in ``value`` by stripping
out duplicates and assigns the array of unique values to
``$variable``. The order of items in the ``value`` array are
preserved.

Examples:
^^^^^^^^^

$one_of_a_kind will be assigned ["a", "b"]

::

    ["unique", "$one_of_a_kind", ["a", "b", "a"]]


--------------------------------------------------------------------------------

regexp
------

``regexp string pattern``

string
    The string the regular expression pattern is applied to.

pattern
    The regular expression pattern.

**regexp** performs a regular expression match against ``string``. The
regular expression pattern syntax is defined by the regular expression
implementation of the language this API is written in.

Pattern groups are a convenient way to select sub-matches. Pattern
groups may accessed by either group number or group name. After a
successful regular expression match the groups are stored in the
special variables ``$regexp_array`` and
``$regexp_map``.

``$regexp_array`` is used to access the groups by
numerical index. Groups are numbered by counting the left parenthesis
group delimiter starting at 1. Group 0 is the entire
match. ``$regexp_array`` is valid irregardless of whether you used
named groups or not.

``$regexp_map`` is used to access the groups by
name. ``$regexp_map`` is only valid if you used named groups in the
pattern.

Examples:
^^^^^^^^^

Many user names are of the form "user@domain", to split the username
from the domain and to be able to work with those values independently
use a regular expression and then assign the results to a variable. In
this example there are two regular expression groups, the first group
is the username and the second group is the domain. In the first
example we use named groups and then access the match information in
the special variable ``$regexp_map`` via the name of the group.

::

    ["regexp", "$assertion[UserName]", "(?P<username>\\w+)@(?P<domain>.+)"],
    ["continue", "if_not_success"],
    ["set", "$username", "$regexp_map[username]"],
    ["set", "$domain", "$regexp_map[domain]"],


This is exactly equivalent but uses numbered groups instead of named
groups. In this instance the group matches are stored in the special
variable ``$regexp_array`` and accessed by numerical index.

::

    ["regexp", "$assertion[UserName]", "(\\w+)@(.+)"],
    ["continue", "if_not_success"],
    ["set", "$username", "$regexp_array[1]"],
    ["set", "$domain", "$regexp_array[2]"],



--------------------------------------------------------------------------------

regexp_replace
--------------

``regexp_replace $variable string pattern replacement``

$variable
    The variable which receives result of the replacement.

string
    The string to perform the replacement on.

pattern
    The regular expression pattern.

replacement
    The replacement specification.

**regexp_replace** replaces each occurrence of ``pattern`` in
``$string`` with ``replacement``. See `regexp`_ for details of using
regular expressions.

Examples:
^^^^^^^^^

Convert hyphens in a name to underscores.

::

    ["regexp_replace", "$name", "$name", "-", "_"]


--------------------------------------------------------------------------------

split
-----

``split $variable string pattern``

$variable
    This variable is assigned an array containing the split items.

string
    The string to split into separate items.

pattern
    The regular expression pattern used to split the string.

**split** splits ``string`` into separate pieces and assigns the
result to ``$variable`` as an array of pieces. The split occurs
wherever the regular expression ``pattern`` occurs in ``string``. See
`regexp`_ for details of using regular expressions.

Examples:
^^^^^^^^^

Split a list of groups separated by a colon (:) into an array of
individual group names. If $assertion[Groups] contained the string
"user:admin" then $group_list will set to ["user", "admin"].

::

    ["split", "$group_list", "$assertion[Groups]", ":"]



--------------------------------------------------------------------------------

join
----

``join $variable array join_string``

$variable
    This variable is assigned the string result of the join operation.

array
    An array of string items to be joined together with
    ``$join_string``.

join_string
    The string inserted between each element in ``array``.

**join** accepts an array of strings and produces a single string
where each element in the array is separated by ``join_string``.

Examples:
^^^^^^^^^

Convert a list of group names into a single string where each group
name is separated by a colon (:). If the array ``$group_list`` is
["user", "admin"] and the ``join_string`` is ":" then the
``$group_string`` variable will be set to "user:admin".

::

    ["join", "$group_string", "$groups", ":"]


--------------------------------------------------------------------------------

lower
-----

``lower $variable value``

$variable
    This variable is assigned the result of the lower operation.

value
    The value to lower case, may be either a string, array, or
    associative array.

**lower** lower cases the input value. The input value may be one of
the following types:

string
    The string is lower cased.

array
    Each member of the array must be a string, the result is an array
    with the items replaced by their lower case value.

associative array
    Each key in the associative array is lower cased. The values
    associated with the key are **not** modified.

Examples:
^^^^^^^^^

Lookup ``UserName`` in the assertion and set the variable
``$username`` to it's lower case value.

::

    ["lower", "$username", "$assertion[UserName]"],

Set each member of the ``$groups`` array to it's lower case value. If
``$groups`` was ["User", "Admin"] then ``$groups`` will become
["user", "admin"].

::

    ["lower", "$groups", "$groups"],

To enable case insensitive lookup's in an associative array lower case
each key in the associative array. If ``$assertion`` was {"UserName":
"JoeUser"} then ``$assertion`` will become {"username": "JoeUser"}

::

    ["lower", "$assertion", $assertion"]

--------------------------------------------------------------------------------

upper
-----

``upper $variable value``

$variable
    This variable is assigned the result of the upper operation.

value
    The value to upper case, may be either a string, array, or
    associative array.

**upper** is exactly analogous to `lower`_ except the values are upper
cased, see `lower`_ for details.


--------------------------------------------------------------------------------

in
--

``in member collection``

member
    The value whose membership is being tested.

collection
    A collection of members. May be string, array or associative array.

**in** tests to see if ``member`` is a member of ``collection``. The
membership test depends on the type of collection, the following are
supported:

array
    If any item in the array is equal to ``member`` then the result is
    success.

associative array
    If the associative array contains a key equal to ``member`` then
    the result is success.

string
    If the string contains a sub-string equal to ``member`` then the
    result is success.

Examples:
^^^^^^^^^

Test to see if the assertion contains a UserName value.

::

    ["in", "UserName", "$assertion"]
    ["continue", "if_not_success"]

Test to see if a group is one of "user" or "admin".

::

    ["in", "$group", ["user", "admin"]]
    ["continue", "if_not_success"]

Test to see if the sub-string "BigCorp" is in
the assertion's ``Provider`` value.

::

    ["in", "BigCorp", "$assertion[Provider]"]
    ["continue", "if_not_success"]


--------------------------------------------------------------------------------

not_in
------

``in member collection``

member
    The value whose membership is being tested.

collection
    A collection of members. May be string, array or associative array.

**not_in** is exactly analogous to `in`_ except the sense of the test
is reversed. See `in`_ for details.

--------------------------------------------------------------------------------

compare
-------

``compare left operator right``

left
    The left hand value of the binary operator.

operator
    The binary operator used for comparing left to right.

right
    The right hand value of the binary operator.


**compare** compares the left value to the right value according the
operator and sets success if the comparison evaluates to True. The
following relational operators are supported.

+----------+-----------------------+
| Operator | Description           |
+==========+=======================+
| ==       | equal                 |
+----------+-----------------------+
| !=       | not equal             |
+----------+-----------------------+
| <        | less than             |
+----------+-----------------------+
| <=       | less than or equal    |
+----------+-----------------------+
| >        | greater than          |
+----------+-----------------------+
| >=       | greater than or equal |
+----------+-----------------------+


The left and right hand sides of the comparison operator *must* be
the same type, no type conversions are performed. Not all combinations
of operator and type are supported. The table below illustrates the
supported combinations. Essentially you can test for equality or
inequality on any type. But only strings and numbers support the
magnitude relational operators.


+----------+--------+---------+------+---------+-----+------+------+
| Operator | STRING | INTEGER | REAL | BOOLEAN | MAP | LIST | NULL |
+==========+========+=========+======+=========+=====+======+======+
| ==       |   X    |    X    |  X   |    X    |  X  |  X   |  X   |
+----------+--------+---------+------+---------+-----+------+------+
| !=       |   X    |    X    |  X   |    X    |  X  |  X   |  X   |
+----------+--------+---------+------+---------+-----+------+------+
| <        |   X    |    X    |  X   |         |     |      |      |
+----------+--------+---------+------+---------+-----+------+------+
| <=       |   X    |    X    |  X   |         |     |      |      |
+----------+--------+---------+------+---------+-----+------+------+
| >        |   X    |    X    |  X   |         |     |      |      |
+----------+--------+---------+------+---------+-----+------+------+
| >=       |   X    |    X    |  X   |         |     |      |      |
+----------+--------+---------+------+---------+-----+------+------+


Examples:
^^^^^^^^^

Test to see if the ``$groups`` array has at least 2 members

::

    ["length", "$group_length", "$groups"],
    ["compare", "$group_length", ">=", 2]


--------------------------------------------------------------------------------

exit
----

``exit status criteria``

status
    The result for the rule.

criteria
    The criteria upon which will cause the rule will be immediately
    exited with a failed status.

**exit** causes the rule being executed to immediately exit and a rule
result if the specified criteria is met. Statement verbs such as `in`_
or `compare`_ set the result status which may be tested with the
``success`` and ``not_success`` criteria.

The exit ``status`` may be one of:

rule_fails
    The rule has failed and no mapping will occur.

rule_succeeds
    The rule succeeded and the mapping will be applied.

The ``criteria`` may be one of:

if_success
    If current result status is success then exit with ``status``.

if_not_success
    If current result status is not success then exit with ``status``.

always
    Unconditionally exit with ``status``.

never
    Effectively a no-op. Useful for debugging.

Examples:
^^^^^^^^^

The rule requires ``UserName`` to be in the assertion.

::

    ["in", "UserName", "$assertion"]
    ["exit", "rule_fails", "if_not_success"]

--------------------------------------------------------------------------------


continue
--------

``continue criteria``

criteria
    The criteria which causes the remainder of the *block* to be
    skipped.

**continue** is used to control execution for statement blocks. It
mirrors in a crude way the `if` expression in a procedural
language. ``continue`` does *not* affect the success or failure of a
rule, rather it controls whether subsequent statements in a block are
executed or not. Control continues at the next statement block.

Statement verbs such as `in`_ or `compare`_ set the result status
which may be tested with the ``success`` and ``not_success`` criteria.

The criteria may be one of:

if_success
    If current result status is success then exit the statement
    block and continue execution at the next statement block.

if_not_success
    If current result status is not success then exit the statement
    block and continue execution at the next statement block.

always
    Immediately exit the statement block and continue execution at the
    next statement block.

never
    Effectively a no-op. Useful for debugging. Execution continues at
    the next statement.

Examples:
^^^^^^^^^

The following pseudo code:

::

    roles = [];
    if ("Groups" in assertion) {
        groups = assertion["Groups"].split(":");
        if ("qa_test" in groups) {
            roles.append("tester");
        }
    }

could be implemented this way:

::

    [
        ["set", "$roles", []],
        ["in", "Groups", "$assertion"],
        ["continue", "if_not_success"],
        ["split" "$groups", $assertion[Groups]", ":"],
        ["in", "qa_test", "$groups"],
        ["continue", "if_not_success"],
        ["append", "$roles", "tester"]
    ]

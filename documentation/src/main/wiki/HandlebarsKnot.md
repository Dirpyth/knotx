# Handlebars Knot
| ! Note |
|:------ |
| @Deprecated: As of release 1.5.0, replaced by [Knot.x Template Engine](https://github.com/Knotx/knotx-template-engine). |

Handlebars Knot is a [[Knot|Knot]] implementation responsible for handlebars template processing.

## How does it work?
Handlebars Knot uses [Handlebars.js](http://handlebarsjs.com/) templating engine. More specifically it utilizes
Handlebars Java port - [Handlebars.java](https://github.com/jknack/handlebars.java) to compile and evaluate
templates.

Handlebars Knot filters Fragments containing `handlebars` in `data-knotx-knots` attribute (see 
[[Knot Election Rule|Knot]]). Then for each Fragment it merges Fragment Content (Handlebars snippet) 
with data from Fragment Context (for example data from external services or form submission response).

### Example
Example Knot Context contains:
*Fragment Content*
```html
<script data-knotx-knots="services,handlebars" data-knotx-service="first-service" type="text/knotx-snippet">
<div class="col-md-4">
  <h2>Snippet1 - {{_result.message}}</h2>
  <div>Snippet1 - {{_result.body.a}}</div>
  {{#string_equals _response.statusCode "200"}}
    <div>Success! Status code : {{_response.statusCode}}</div>
  {{/string_equals}}
</div>
</script>
```
*Fragment Context*
```json
{
  "_result": {
    "message":"this is webservice no. 1",
    "body": {
      "a": "message a"
    }
  },
  "_response": {
    "statusCode":"200"
  }
}
```
Handlebars Knot uses data from Fragment Context and applies it to Fragment Content:
```html
<div class="col-md-4">
  <h2>Snippet1 - this is webservice no. 1</h2>
  <div>Snippet1 - message a</div>
  <div>Success! Status code : 200</div>
</div>
```
Finally Fragment Content is replaced with merged result.

## Interpolation symbol
By default , the Handlebars engine uses `{{` and `}}` symbols as tag delimiters.
However, while Knot.x can be used to generate markup on the server side, the very same page might also contain templates intended for client-side processing. 
This is often the case when frameworks like Angular.js or Vue.js are used.
To avoid conflicts between Mustache templates to be executed server-side and ones evaluated on the client side, a Handlebars knot introduces two configuration parameters that enable you to configure custom symbols to be used in Knot.x snippets.
E.g.:
In order to use different symbols as below
```html
<div class="col-md-4">
  <h2>Snippet1 - {<_result.message>}</h2>
</div>
```
You can reconfigure an engine as follows in the Handlebars Knot section:
```hocon
config {
  startDelimiter = "{<"
  endDelimiter = ">}"
}
```

## How to configure?
For all configuration fields and their defaults consult [HandlebarsKnotOptions](https://github.com/Cognifide/knotx/blob/master/documentation/src/main/cheatsheet/cheatsheets.adoc#handlebarsknotoptions)

In general, it:
- Listens on event bus address 'knotx.knot.handlebars'
- Renders HTML debug comments on the output HTML

## How to extend?

### Extending handlebars with custom helpers

If the list of available handlebars helpers is not enough, you can easily extend it. To do this the 
following actions should be undertaken:

1. Use io.knotx:knotx-knot-handlebars module as dependency
2. Create a class implementing ```io.knotx.knot.templating.handlebars.CustomHandlebarsHelper``` interface. 
This interface extends [com.github.jknack.handlebars.Helper](https://jknack.github.io/handlebars.java/helpers.html)
3. Register the implementation as a service in the JAR file containing the implementation
    * Create a configuration file called META-INF/services/io.knotx.handlebars.CustomHandlebarsHelper 
    in the same project as your implementation class
    * Paste a fully qualified name of the implementation class inside the configuration file. If you're 
    providing multiple helpers in a single JAR, you can list them in new lines (one name per line is allowed) 
    * Make sure the configuration file is part of the JAR file containing the implementation class(es)
3. Run Knot.x with the JAR file in the classpath

#### Example extension

Sample application contains an example custom Handlebars helper - please take a look at the implementation of ```BoldHelper```:
* Implementation class: ```io.knotx.example.monolith.handlebars.BoldHelper```
* service registration: ```knotx-example/knotx-example-app/src/main/resources/META-INF/services/io.knotx.knot.templating.handlebars.CustomHandlebarsHelper```

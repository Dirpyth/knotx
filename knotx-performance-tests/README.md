# Knot.x performance tests

## Scenarios

### 1 snippet 1 service

- [http://localhost:8092/content/simple-1snippet-1service.html](http://localhost:8092/content/simple-1snippet-1service.html)

This scenario represents the case that 80% of all dynamic pages might be. The page needs only one external
data source to be rendered.

### 1 snippet 5 services

- [http://localhost:8092/content/simple-1snippet-5services.html](http://localhost:8092/content/simple-1snippet-5services.html)

This is also quite common scenario, where a page contains 1 dynamic snippet which presents the data 
from 5 different external endpoints.

### 5 snippets 1 service for each snippet

- [http://localhost:8092/content/simple-5snippets.html](http://localhost:8092/content/simple-5snippets.html)

In this scenario, there are 5 dynamic, separate snippets where each of those snippets requires connection
to a different external endpoint.

### 10 snippets 1 service for each

- [http://localhost:8092/content/simple-10snippets.html](http://localhost:8092/content/simple-10snippets.html)

More complex example to the previous one, there are 10 snippets.

### heavy template

- [http://localhost:8092/content/simple-big-data.html](http://localhost:8092/content/simple-big-data.html)

In this scenario there is only one snippet that uses one external service, however the whole template
is big and heavy.


### 100 small snippets with huge json to process

- [http://localhost:8092/content/100-small-snippets-1-service-wtih-big-json.html](http://localhost:8092/content/100-small-snippets-1-service-wtih-big-json.html)

In this scenario, there are 100 small snippets where each uses a service that returns a big json, that
later is passed to templating engine. This scenario is focused on testing performance of templating
engine.

### 1 big snippet with huge json to process

- [http://localhost:8092/content/1-big-snippet-1-service-wtih-big-json.html](http://localhost:8092/content/1-big-snippet-1-service-wtih-big-json.html)

This scenario is variation of a previous one. All snippets were merged into one big snippet since
all of them use the same external service. This is still heavy to process by templating engine.


### soak test

Mix of all pages with some probable distribution of requests. It lasts 24-48 hours.

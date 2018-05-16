# Arachne Web Crawler
The goal of this project was to create a flexible, scalable and easily maintainable all-purpose Web Crawler while familiarizing
myself with core data mining concepts. The idea was to create a generic and configurable implementation, to allow it
to serve as an independant development base for projects prior to requiring an efficient and specialized Crawler implementation 
for production.

## Implementation Notes
-The controller class initializes instances of the crawler, and distributes all seeds evenly across the crawlers. The crawler class visits web pages based on a starting URL and a regular expression, and processes web pages based on another regular expression before returning raw JSON data to the controller to be produced to a Kafka topic. All these details such as the shouldVisit and shouldProcess regular expressions, target topic, starting URLs and processing logic are all defined within a Seed class.
-The crawler has a few configurable properties. It can continuously rotate between all it's seeds, or process them all only once using the "continuous" property. It can choose to only process pages once but continuously visit the seeds using the "single.pass" property, which is useful for periodically crawling the same page for new posts/listings. The thread count, and certain Kafka producer properties are also exposed through the properties file.


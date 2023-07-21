# ChatBot Commerce on LINE Messaging Platform with Spring Boot


It's E-Commerce assistance for placing orders from the LINE Messaging platform.
Users can see the products and can be purchased them.

This ChatBot will help you to expand your business with less human integration. 

## ChatBot Development / Integration procedure 

- Create a Business Account
- Create a Channel on LINE
- Collect the Channel Secret key
- Set Rich Menu by calling LINE API
- Insert record on line_config table with the required information
- Insert products on product_table (you may run product.sql)
- Subscribe to your LINE Channel from any personal LINE account
- Set webhook URL on LINE of your Spring Boot application server URL
- Enjoy E-Commerce order with ChatBot

## Technologies

- Java 17
- Spring Boot 3x with Thymleaf
- Gradle

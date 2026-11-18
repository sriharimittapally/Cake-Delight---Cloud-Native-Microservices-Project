
-- Select the Cake Catalog database
USE cake_catalog_db;

-- Create the cakes table
CREATE TABLE IF NOT EXISTS cakes (

    id BIGINT NOT NULL AUTO_INCREMENT,

    name VARCHAR(150) NOT NULL,

    description TEXT,

    category VARCHAR(100) NOT NULL,

    price DECIMAL(10, 2) NOT NULL,

    available BOOLEAN NOT NULL DEFAULT TRUE,

    image_reference VARCHAR(500),

    PRIMARY KEY (id)

);


USE cake_catalog_db;

-- Insert Chocolate Cake if it does not already exist.
INSERT INTO cakes
(name, description, category, price, available, image_reference)
SELECT
'Chocolate Cake',
'Rich chocolate cake with chocolate cream.',
'Birthday',
500.00,
TRUE,
'https://th.bing.com/th/id/OIP.o-7bwG7tJYPS-qBYzQUwNgHaHa?w=182&h=182&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3'
WHERE NOT EXISTS (
    SELECT 1 FROM cakes
    WHERE name = 'Chocolate Cake'
    AND category = 'Birthday'
);


-- Insert Vanilla Cake if it does not already exist.
INSERT INTO cakes
(name, description, category, price, available, image_reference)
SELECT
'Vanilla Cake',
'Soft vanilla sponge cake with vanilla frosting.',
'Birthday',
400.00,
TRUE,
'https://th.bing.com/th/id/OIP.4uaeFFLcZ7-5DFMdjji71gHaHa?w=182&h=182&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3'
WHERE NOT EXISTS (
    SELECT 1 FROM cakes
    WHERE name = 'Vanilla Cake'
    AND category = 'Birthday'
);


-- Insert Black Forest Cake if it does not already exist.
INSERT INTO cakes
(name, description, category, price, available, image_reference)
SELECT
'Black Forest Cake',
'Chocolate sponge cake with cream and cherries.',
'Anniversary',
650.00,
TRUE,
'https://th.bing.com/th/id/OIP.gGOedd1gxGojCHF7BDLuHwHaHa?w=182&h=182&c=7&r=0&o=7&dpr=1.5&pid=1.7&rm=3'
WHERE NOT EXISTS (
    SELECT 1 FROM cakes
    WHERE name = 'Black Forest Cake'
    AND category = 'Anniversary'
);


-- Insert Red Velvet Cake if it does not already exist.
INSERT INTO cakes
(name, description, category, price, available, image_reference)
SELECT
'Red Velvet Cake',
'Red velvet sponge cake with cream cheese frosting.',
'Special',
800.00,
TRUE,
'https://res.cloudinary.com/dflkpq2y2/image/upload/v1734452694/Red_Velvet_Cake12_fsftox.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM cakes
    WHERE name = 'Red Velvet Cake'
    AND category = 'Special'
);


-- Insert Strawberry Cake if it does not already exist.
INSERT INTO cakes
(name, description, category, price, available, image_reference)
SELECT
'Strawberry Cake',
'Fresh strawberry cake with strawberry cream.',
'Birthday',
550.00,
FALSE,
'https://preppykitchen.com/wp-content/uploads/2022/05/Strawberry-Cake-Feature.jpg'
WHERE NOT EXISTS (
    SELECT 1 FROM cakes
    WHERE name = 'Strawberry Cake'
    AND category = 'Birthday'
);

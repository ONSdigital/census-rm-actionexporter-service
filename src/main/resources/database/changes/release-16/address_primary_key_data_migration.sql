-- NOT TO BE INCLUDED IN LIQUIBASE CHANGES
-- TO BE RUN MANUALLY AFTER RELEASE-16 SCHEMA CHANGES
UPDATE actionexporter.actionrequest AS ar
SET addressFK = a.addressPK
FROM actionexporter.address AS a
WHERE ar.sampleunitref = a.sampleunitref;
--WHAT IS THIS SCARY THING AND DO WE STILL NEED IT, I've never run it

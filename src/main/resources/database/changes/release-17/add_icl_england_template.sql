INSERT INTO actionexporter.template (templatenamepk, content, datemodified) VALUES ('census_icl_en',
'<#list actionRequests as actionRequest>' ||
'${(actionRequest.iac?trim)!"null"}|' ||
'${(actionRequest.caseRef?trim)!"null"}|' ||
'${(actionRequest.address.line1?trim)!}|' ||
'${(actionRequest.address.line2?trim)!}|' ||
'${(actionRequest.address.line3?trim)!}|' ||
'${(actionRequest.address.townName?trim)!}|' ||
'${(actionRequest.address.postcode?trim)!}|' ||
'${(actionRequest.address.sampleUnitRef)!"null"}|' ||
'PACK_CODE
</#list>'
,
now());


INSERT INTO actionexporter.templatemapping
(actiontypenamepk, templatenamefk, filenameprefix, datemodified)
VALUES
('CENSUS_ICL','census_icl_en', 'CENSUS_ICL', now());

-- UPDATE actionexporter.templatemapping SET templatenamefk = 'census_icl_en' WHERE actiontypenamepk = 'census_icl_en';

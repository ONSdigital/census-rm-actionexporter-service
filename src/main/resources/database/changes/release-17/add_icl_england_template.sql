INSERT INTO actionexporter.template (templatenamepk, content, datemodified) VALUES ('iclEngland',
'<#list actionRequests as actionRequest>' ||
'${(actionRequest.iac?trim)!"null"}|' ||
'ARID' ||
'${(actionRequest.address.line1?trim)!}|' ||
'${(actionRequest.address.line2?trim)!}|' ||
'ADDRESS LINE 3|' ||
'${(actionRequest.address.townName?trim)!}|' ||
'${(actionRequest.address.postcode?trim)!}|' ||
'${(actionRequest.address.sampleUnitRef)!"null"}|' ||
'PACK_CODE
</#list>'
,
now());


UPDATE actionexporter.templatemapping SET templatenamefk = 'iclEngland' WHERE actiontypenamepk = 'ICL_ENGLAND';
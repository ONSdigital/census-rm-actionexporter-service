UPDATE actionexporter.template
SET content =
'<#list actionRequests as actionRequest>' ||
'${(actionRequest.iac?trim)!}|' ||
'${(actionRequest.contact.title?trim)!}|' ||
'${(actionRequest.contact.forename?trim)!}|' ||
'${(actionRequest.contact.surname?trim)!}|' ||
'${(actionRequest.caseRef?trim)!}|' ||
'${(actionRequest.address.line1?trim)!}|' ||
'${(actionRequest.address.line2?trim)!}|' ||
'${(actionRequest.address.line3?trim)!}|' ||
'${(actionRequest.address.townName?trim)!}|' ||
'${(actionRequest.address.postcode?trim)!}|' ||
'P_IC_ICL1
</#list>', datemodified =now()
WHERE templatenamepk = 'icl1e'




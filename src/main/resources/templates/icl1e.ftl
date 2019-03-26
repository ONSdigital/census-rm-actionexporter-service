<#list actionRequests as actionRequest>
${(actionRequest.iac?trim)!"null"}|${(actionRequest.caseRef?trim)!"null"}|${(actionRequest.address.line1?trim)!}|${(actionRequest.address.line2?trim)!}|${(actionRequest.address.line3?trim)!}|${(actionRequest.address.townName?trim)!}|${(actionRequest.address.postcode?trim)!}|${(actionRequest.address.sampleUnitRef)!"null"}|PACK_CODE
</#list>
INSERT INTO actionexporter.template
(templatenamepk, content, datemodified)
VALUES ('icl2w',
        '<#list actionRequests as actionRequest>' ||
        '${(actionRequest.iac?trim)!}|' ||
        '${(actionRequest.qid?trim)!}|' ||
        '${(actionRequest.iacWales?trim)!}|' ||
        '${(actionRequest.qidWales?trim)!}|' ||
        '${(actionRequest.caseRef?trim)!}|' ||
        '${(actionRequest.address.line1?trim)!}|' ||
        '${(actionRequest.address.line2?trim)!}|' ||
        '${(actionRequest.address.line3?trim)!}|' ||
        '${(actionRequest.address.townName?trim)!}|' ||
        '${(actionRequest.address.postcode?trim)!}|' ||
        'P_IC_ICL2
        </#list>',
        now());


INSERT INTO actionexporter.templatemapping
(actiontypenamepk, templatenamefk, filenameprefix, datemodified, requesttype)
VALUES
('ICL2W','icl2w', 'P_IC_ICL2', now(), 'print_service');



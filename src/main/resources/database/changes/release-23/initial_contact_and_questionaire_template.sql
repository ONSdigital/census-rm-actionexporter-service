
INSERT INTO actionexporter.template
(templatenamepk, content, datemodified)
VALUES ('initial_contact',
        '<#list actionRequests as actionRequest>' ||
        '${(actionRequest.iac?trim)!}|' ||
        '${(actionRequest.caseRef?trim)!}|' ||
        '${(actionRequest.contact.title?trim)!}|' ||
        '${(actionRequest.contact.forename?trim)!}|' ||
        '${(actionRequest.contact.surname?trim)!}|' ||
        '${(actionRequest.address.line1?trim)!}|' ||
        '${(actionRequest.address.line2?trim)!}|' ||
        '${(actionRequest.address.line3?trim)!}|' ||
        '${(actionRequest.address.townName?trim)!}|' ||
        '${(actionRequest.address.postcode?trim)!}|' ||
        '${(actionRequest.packCode?trim)!}
        </#list>',
        now());

-- England
INSERT INTO actionexporter.templatemapping
(actiontypenamepk, templatenamefk, filenameprefix, datemodified, requesttype)
VALUES
('ICL1E','initial_contact', 'P_IC_ICL1', now(), 'print_service');

-- Wales
INSERT INTO actionexporter.templatemapping
(actiontypenamepk, templatenamefk, filenameprefix, datemodified, requesttype)
VALUES
('ICL2W','initial_contact', 'P_IC_ICL2', now(), 'print_service');

-- Nireland
INSERT INTO actionexporter.templatemapping
(actiontypenamepk, templatenamefk, filenameprefix, datemodified, requesttype)
VALUES
('ICL4E','initial_contact', 'IC_ICL4', now(), 'print_service');

--------------
--- Questionaire
-------------

INSERT INTO actionexporter.template
(templatenamepk, content, datemodified)
VALUES ('questionaire',
        '<#list actionRequests as actionRequest>' ||
        '${(actionRequest.iac?trim)!}|' ||
        '${(actionRequest.qid?trim)!}|' ||
        '${(actionRequest.iacWales?trim)!}|' ||
        '${(actionRequest.qidWales?trim)!}|' ||
        '${(actionRequest.caseRef?trim)!}|' ||
        '${(actionRequest.contact.title?trim)!}|' ||
        '${(actionRequest.contact.forename?trim)!}|' ||
        '${(actionRequest.contact.surname?trim)!}|' ||
        '${(actionRequest.address.line1?trim)!}|' ||
        '${(actionRequest.address.line2?trim)!}|' ||
        '${(actionRequest.address.line3?trim)!}|' ||
        '${(actionRequest.address.townName?trim)!}|' ||
        '${(actionRequest.address.postcode?trim)!}|' ||
        '${(actionRequest.packCode?trim)!}
         </#list>',
         now());

-- England
INSERT INTO actionexporter.templatemapping
(actiontypenamepk, templatenamefk, filenameprefix, datemodified, requesttype)
VALUES
('ICHHQE','questionaire', 'P_IC_H1', now(), 'print_service');

-- Wales
INSERT INTO actionexporter.templatemapping
(actiontypenamepk, templatenamefk, filenameprefix, datemodified, requesttype)
VALUES
('ICHHQW','questionaire', 'P_IC_H2', now(), 'print_service');

-- Nireland
INSERT INTO actionexporter.templatemapping
(actiontypenamepk, templatenamefk, filenameprefix, datemodified, requesttype)
VALUES
('ICHHQN','questionaire', 'P_IC_H4', now(), 'print_service');

ALTER TABLE actionexporter.actionrequest
ADD COLUMN packcode character varying (24);
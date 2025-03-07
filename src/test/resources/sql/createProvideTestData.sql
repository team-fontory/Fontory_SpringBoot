truncate table `member`;
truncate table `provide`;
insert into `provide` (`provide_id`, `provider`, `provided_id`, `email`, `member_id`)
values (1, 'GOOGLE', 'test_provided_id', 'test_email', NULL);
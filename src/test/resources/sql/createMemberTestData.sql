-- insert into `member` (`member_id`, `nickname`, `gender`, `birth`, `terms`, `profile_image`, `provide_id`)
-- values (1, 'testEmail', 'MALE', '2025-01-26', 1, 'testImage', 1)

insert into `provide` (`provide_id`, `provider`, `provided_id`, `email`, `member_id`)
values (1, 'GOOGLE', 'test_provided_id', 'test_email', NULL);
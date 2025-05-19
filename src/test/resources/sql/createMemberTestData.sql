truncate table `member`;
truncate table `provide`;

insert into `member` (`member_id`, `nickname`, `gender`, `birth`, `profile_image_key`, `provide_id`, `status`,`created_at`, `updated_at`)
values (999, 'testMemberNickName', 'MALE', '2025-01-26', 'testMemberProfileImage', 1, 'ACTIVATE','1922-09-18 19:11:00.000000', '1922-09-18 19:11:00.000000');

insert into `provide` (`provide_id`, `provider`, `provided_id`, `email`, `member_id`)
values (1, 'GOOGLE', 'testMemberProvidedId', 'testMemberEmail', 999);
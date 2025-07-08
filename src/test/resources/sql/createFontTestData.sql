truncate table `font`;
truncate table `member`;
truncate table `provide`;

INSERT INTO `font` (
    `font_id`,
    `name`,
    `status`,
    `example`,
    `download_count`,
    `bookmark_count`,
    `file_key`,
    `member_id`,
    `created_at`,
    `updated_at`
) VALUES (
    999,
    '테스트폰트',
    'DONE',
    '이것은 테스트용 예제입니다.',
    0,
    0,
    'key',
    999,
    '2025-04-08 10:00:00',
    '2025-04-08 10:00:00'
);

insert into `member` (`member_id`, `nickname`, `gender`, `birth`, `provide_id`, `status`, `created_at`, `updated_at`)
values (999, 'existMemberNickName', 'MALE', '2025-01-26', 999, 'ACTIVATE','1922-09-18 19:11:00.000000', '1922-09-18 19:11:00.000000');

insert into `member` (`member_id`, `nickname`, `gender`, `birth`, `provide_id`, `status`, `created_at`, `updated_at`)
values (1, 'createdMemberNickName', 'MALE', '2025-01-26', 1, 'ACTIVATE','1922-09-18 19:11:00.000000', '1922-09-18 19:11:00.000000');

insert into `provide` (`provide_id`, `provider`, `provided_id`, `email`, `member_id`)
values (999, 'GOOGLE', 'test_provided_id', 'test_email', 999);

insert into `provide` (`provide_id`, `provider`, `provided_id`, `email`, `member_id`)
values (1, 'GOOGLE', 'test_provided_id', 'test_email', 1);
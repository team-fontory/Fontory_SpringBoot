-- Clean up tables first to ensure consistent test state
truncate table `bookmark`;
truncate table `font`;
truncate table `member`;
truncate table `provide`;

-- Insert test member (reused from existing test data)
insert into `member` (`member_id`, `nickname`, `gender`, `birth`, `provide_id`, `status`, `created_at`, `updated_at`)
values (999, 'testMemberNickName', 'MALE', '2025-01-26', 1, 'ACTIVATE','2025-01-18 19:11:00.000000', '2025-01-18 19:11:00.000000');

insert into `provide` (`provide_id`, `provider`, `provided_id`, `email`, `member_id`)
values (1, 'GOOGLE', 'testMemberProvidedId', 'testMemberEmail', 999);

-- Insert test fonts for bookmark testing
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
    1,  -- This font has 1 bookmark
    'test-font-key-999',
    999,
    '2025-04-08 10:00:00',
    '2025-04-08 10:00:00'
);

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
    998,
    '테스트폰트2',
    'DONE',
    '이것은 두번째 테스트용 폰트입니다.',
    0,
    0,  -- This font has no bookmarks
    'test-font-key-998',
    999,
    '2025-04-08 11:00:00',
    '2025-04-08 11:00:00'
);

-- Insert existing bookmark for font 999
INSERT INTO `bookmark` (
    `bookmark_id`,
    `member_id`,
    `font_id`,
    `created_at`,
    `updated_at`
) VALUES (
    1,
    999,
    999,
    '2025-04-08 12:00:00',
    '2025-04-08 12:00:00'
);
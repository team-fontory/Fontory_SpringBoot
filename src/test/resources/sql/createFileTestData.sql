truncate table `file`;
truncate table `member`;
truncate table `provide`;

-- Test member data
insert into `member` (`member_id`, `nickname`, `gender`, `birth`, `terms`, `profile_image_key`, `provide_id`, `created_at`, `updated_at`)
values (999, 'existMemberNickName', 'MALE', '2025-01-26', 1, 'existMemberProfileImage', 999, NOW(), NOW());

-- Test provide data
insert into `provide` (`provide_id`, `provider`, `provided_id`, `email`, `member_id`)
values (999, 'GOOGLE', 'test_provided_id', 'test_email', 999);

-- Test file metadata
insert into `file` (
    `file_id`,
    `file_name`,
    `file_type`,
    `extension`,
    `file_key`,
    `uploader_id`,
    `size`,
    `uploaded_at`,
    `created_at`,
    `updated_at`
) values (
             999,
             'testfile.jpg',
             'PROFILE_IMAGE',
             'jpg',
             'test-profile-fileKey',
             999,
             1024,
             NOW(),
             NOW(),
             NOW()
         );

insert into `file` (
    `file_id`,
    `file_name`,
    `file_type`,
    `extension`,
    `file_key`,
    `uploader_id`,
    `size`,
    `uploaded_at`,
    `created_at`,
    `updated_at`
) values (
             998,
             'testfont.jpg',
             'FONT_PAPER',
             'jpg',
             'test-font-fileKey',
             999,
             2048,
             NOW(),
             NOW(),
             NOW()
         );
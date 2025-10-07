-- Clean up test data in correct order (due to foreign key constraints)
truncate table `bookmark`;
truncate table `font`;
truncate table `member`;
truncate table `provide`;
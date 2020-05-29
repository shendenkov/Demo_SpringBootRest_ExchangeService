This is **DEMO** project for exchange service REST API implemented on SpringBoot.

**Firstly**: To work, you need a database, the settings of which can be set using environment variables:
- `DATABASE_URL` (For example, `jdbc:mysql://localhost:3306/demo_database?createDatabaseIfNotExist=true&serverTimezone=UTC`)
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

Database scheme described in `initDB.sql` file.

**Secondly**: You should have users in table `service_users`with **PLAIN** passwords and corresponding roles (`USER` | `ADMIN`)

**Only ADMIN** can set commissions and rates.
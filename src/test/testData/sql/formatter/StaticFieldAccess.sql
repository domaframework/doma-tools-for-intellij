SELECT *
FROM configurations
WHERE
config_key = /*@ com.example.Constants @CONFIG_KEY */'system.timeout'
  AND value = /*@ com.example.Utils @ util. getDefaultValue () */'30'
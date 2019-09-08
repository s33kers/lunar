# Lunar - Github API 

## API
### Fetch top 10 Java frameworks
If Authentication header with Github basic login token is provided then shows if that repository is stared by user
```
GET /git
```

### Star repository. 
Authentication header with Github basic login token must be provided
```
PUT /git/{owner}/repo
```

### Unstar repository. 
Authentication header with Github basic login token must be provided

```
DELETE /git/{owner}/repo
```

## Running application

### Docker
Docker compose file is provided with image from Docker Hub. 
```
docker-compose up
```
To build newest Docker image from code run gradle task `buildDocker`
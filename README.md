# Shareit

Sharing service.
<br>
Shareit provides a REST API for sharing items:
- Users can share items using `/items` endpoints
- They can book those items using `/bookings`, item owners can reject or approve those bookings
- If user didn't find the desired item he can create request (`/requets`) and others can share their items as a response to this request
- Users can leave comments about how the like using and item with `/items/comments` endpoint

## Requirements

`maven 3.8.1+`, `docker 24.0.2+`, `docker-compose 1.29.2+`

## How to run

In the project directory:

1. `mvn clean install -DskipTests`
2. `TZ='<your time zone>' docker-compose up`, e.g. `TZ='Asia/Barnaul' docker-compose up`
3. Send http requests to port 8080, e.g. `http://localhost:8080/users`

## Testing
You can test the app using postman collection in `postman-tests.json` file.

## Endpoints

**Note:** if response status is not 2xx, the body will contain the following object:

```json
{
  "error": "Error message",
  "objectValidationViolations": [
    {
      "message": "object validation violation message"
    }
  ],
  "fieldValidationViolations": [
    {
      "field": "name of the field",
      "message": "validation violation message"
    }
  ],
  "httpAttributeValidationViolations": [
    {
      "name": "http attribute (request param, header, etc.)",
      "message": "validation violation message"
    }
  ]
}
```

### /users

#### Create new user

```http request
POST http://localhost:8080/users
Content-Type: application/json
```

```json
{
  "name": "John Doe", //not null
  "email": "johndoe@me.com"//not null, well-formed email, unique
}
```

##### Responses

- **200:**

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "johndoe@me.com"
}
```

- **400:** Validation violation
- **409:** User with this email already exists
- **500:** Internal server error

---

#### Get all users

```http request
GET http://localhost:8080/users
```

##### Responses

- **200:**

```json
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "johndoe@me.com"
  },
  {
    "id": 2,
    "name": "Jane Doe",
    "email": "janedoe@me.com"
  }
]
```

- **500:** Internal server error

---

#### Get user by id

```http request
GET http://localhost:8080/users/{id}
```

##### Responses

- **200:**

```json
{
  "id": 1,
  "name": "John Doe",
  "email": "johndoe@me.com"
}
```

- **404:** User not found
- **500:** Internal server error

---

#### Update user by id

```http request
PATCH http://localhost:8080/users/{id}
Content-Type: application/json
```

```json
{
  "name": "Updated John Doe", //might be null
  "email": "updatedjohndoe@me.com"//might be null, well-fomed email, unique
}
```

##### Responses

- **200:**

```json
{
  "id": 1,
  "name": "Updated John Doe",
  "email": "updatedjohndoe@me.com"
}
```

- **400:** Validation failed
- **404:** User not found
- **500:** Internal server error

---

#### Delete user by id

```http request
DELETE http://localhost:8080/users/{id}
```

##### Responses

- **200:** User deleted
- **500:** Internal server error
---

### /items

#### Create new Item
```http request
POST http://localhost:8080/items
Content-Type: application/json
X-Sharer-User-Id: 1
```
```json
{
  "name": "hammer drill", //not null, min=1, max=50
  "description": "Hammer drill 'Whirlwind DU-850',equipped with a reverse function", //not null, min=1, max=200
  "requestId": 1 //might be null
}
```

##### Responses
- **200:** Item created
```json
{
  "id": 1,
  "name": "hammer drill", //not null, min=1, max=50
  "description": "Hammer drill equipped with a reverse function", //not null, min=1, max=200
  "available": true, //not null
  "requestId": 1 //might be null
}
```
- **400:** Validation failed
- **404:** User or request not found
- **500:** Internal server error
---

#### Get Item by id
Only owner will be able to see last and next bookings, if the item wasn't booked, they will be null
```http request
GET http://localhost:8080/items/{itemId}
X-Sharer-User-Id: 1
```

##### Responses
- **200:**

```json
{
  "id": 1,
  "name": "hammer drill",
  "description": "Hammer drill equipped with a reverse function",
  "available": true,
  "lastBooking": {
    "id": 1,
    "start": "2023-06-20T11:24:02",
    "end": "2023-06-27T11:24:02",
    "bookerId": 2,
    "itemId": 1,
    "status": "APPROVED"
  },
  "nextBooking": null,
  "comments": [
    {
      "id": 1,
      "text": "Very good drill",
      "authorName": "Jane doe",
      "created": "2023-06-27T15:00:00"
    }
  ]
}
```
- **404:** User or item not found
- **500:** Internal server error
---

#### Search items by name or description
Request params:
- `text` - text to be searched, if null an empty list will be returned
- `from` - index of an item from which to start, _default = 0_
- `size` - number fo items to search, _default = 10_
```http request
GET http://localhost:8080/items/search?text={text}&from=0&size=10
```
##### Responses
- **200:**
```json
[
    {
      "id": 1,
      "name": "hammer drill",
      "description": "Hammer drill equipped with a reverse function",
      "available": true,
      "requestId": 1
    }
]
```
- **400:** Request param's validation failed
- **500:** Internal server error
---

#### Get all by owner id
Request params:
- `text` - text to be searched, if null an empty list will be returned
- `from` - index of an item from which to start, _default = 0_
- `size` - number fo items to search, _default = 10_
```http request
GET http://localhost:8080/items/
X-Sharer-User-Id: 1
```
##### Responses
- **200:**
```json
[
  {
    "id": 1,
    "name": "hammer drill",
    "description": "Hammer drill equipped with a reverse function",
    "available": true,
    "lastBooking": {
      "id": 1,
      "start": "2023-06-20T11:24:02",
      "end": "2023-06-27T11:24:02",
      "bookerId": 2,
      "itemId": 1,
      "status": "APPROVED"
    },
    "nextBooking": null,
    "comments": [
      {
        "id": 1,
        "text": "Very good drill",
        "authorName": "Jane doe",
        "created": "2023-06-27T15:00:00"
      }
    ]
  }
]
```
- **400:** Request param's validation failed
- **500:** Internal server error
---

#### Update item by id
Only owner can update the item
```http request
PATCH http://localhost:8080/{itemId}
X-Sharer-User-Id: 1
```
```json
{
  "name": "updated hammer drill", //might be null, min=1, max=50
  "description": "Hammer drill 'Whirlwind DU-850',equipped with a reverse function", //might be null, min=1, max=200
  "requestId": 1 //might be null
}
```
##### Responses
- **200:**
```json
{
  "id": 1,
  "name": "updated hammer drill",
  "description": "Hammer drill 'Whirlwind DU-850',equipped with a reverse function",
  "requestId": 1
}
```
- **400:** Validation failed
- **404:** Item, request or user not found, or user is not owner
- **500:** Internal server error
---

#### Add comment to an item
Comments can only be added by a user who booked the item and this booking already ended
```http request
POST http://localhost:8080/{itemid}/comment
X-Sharer-User-Id: 1
```
```json
{
  "text": "Very good drill" //not blank
}
```

##### Responses
- **200:**
```json
{
  "id": 1,
  "text": "Very good drill",
  "authorName": "Jane Doe",
  "created": "2023-06-27T15:00:00"
}
```
- **400:** Validation failed or user cannot comment this item
- **404:** User or item not found
- **500:** Internal server error
---


### /bookings
#### Book an item
```http request
POST http://localhost:8080/bookings
X-Sharer-User-Id: 1
```
```json
{
  "start": "2023-06-20T11:24:02", //not null, future or present
  "end": "2023-06-27T11:24:02", //not null, future
  "itemId": 1
}
```

##### Responses
- **200:**
```json
{
  "id": 1,
  "start": "2023-06-20T11:24:02",
  "end": "2023-06-27T11:24:02",
  "booker": {
    "id": 2,
    "name": "Jane Doe",
    "email": "janedoe@mail.me"
  },
  "item": {
    "id": 1,
    "name": "hammer drill",
    "description": "Hammer drill equipped with a reverse function",
    "available": true,
    "requestId": 1
  },
  "status": "WAITING"
}
```
- **400:** Validation failed, item unavailable,
- **404:** User or item not found or item owner id is the same as booker id
- **500:** Internal server error
---

#### Change booking status
Request param `approved` of type boolean indicates if booking was approved by item's owner
```http request
PATCH http://localhost:8080/bookings/{bookingId}?approved={approved}
X-Sharer-User-Id: 1
```
##### Responses
- **200:**
```json
{
  "id": 1,
  "start": "2023-06-20T11:24:02",
  "end": "2023-06-27T11:24:02",
  "booker": {
    "id": 2,
    "name": "Jane Doe",
    "email": "janedoe@mail.me"
  },
  "item": {
    "id": 1,
    "name": "hammer drill",
    "description": "Hammer drill equipped with a reverse function",
    "available": true,
    "requestId": 1
  },
  "status": "APPROVED"
}
```
- **400:** Validation failed or booking was already approved
- **404:** Booking, user or item not found or user is not the booker
- **500:** Internal server error
---

#### Get all bookings by booker id sorted by start time desc
Request params:
- `state` - state of bookings to get. State can be the following:
  - `All` - default 
  - `CURRENT` - bookings that haven't ended yet
  - `PAST` - bookings that happened in past
  - `FUTURE` - bookings that will happen in future
  - `WAITING` - bookings that wasn't approved yet
  - `REJECTED` - bookings, that wasn't approved
- `from` - index of an item from which to start, _default = 0_
- `size` - number fo items to search, _default = 10_
```http request
GET http://localhost:8080/bookings?state={state}&from={from}&size={size}
X-Sharer-User-Id: 2
```
##### Responses
- **200:** 
```json
[
  {
    "id": 1,
    "start": "2023-06-20T11:24:02",
    "end": "2023-06-27T11:24:02",
    "booker": {
      "id": 2,
      "name": "Jane Doe",
      "email": "janedoe@mail.me"
    },
    "item": {
      "id": 1,
      "name": "hammer drill",
      "description": "Hammer drill equipped with a reverse function",
      "available": true,
      "requestId": 1
    },
    "status": "APPROVED"
  }
]
```
- **400:** Validation failed
- **404:** User not found
- **500:** Internal server error
---

#### Get all by item owner id sorted by start time desc
Request params:
- `state` - state of bookings to get. State can be the following:
  - `All` - default
  - `CURRENT` - bookings that haven't ended yet
  - `PAST` - bookings that happened in past
  - `FUTURE` - bookings that will happen in future
  - `WAITING` - bookings that wasn't approved yet
  - `REJECTED` - bookings, that wasn't approved
- `from` - index of an item from which to start, _default = 0_
- `size` - number fo items to search, _default = 10_
```http request
GET http://localhost:8080/bookings/owner?state={state}&from={from}&size={size}
X-Sharer-User-Id: 1
```
##### Responses
- **200:**
```json
[
  {
    "id": 1,
    "start": "2023-06-20T11:24:02",
    "end": "2023-06-27T11:24:02",
    "booker": {
      "id": 2,
      "name": "Jane Doe",
      "email": "janedoe@mail.me"
    },
    "item": {
      "id": 1,
      "name": "hammer drill",
      "description": "Hammer drill equipped with a reverse function",
      "available": true,
      "requestId": 1
    },
    "status": "APPROVED"
  }
]
```
- **400:** Validation failed
- **404:** User not found
- **500:** Internal server error
---

#### Get booking by id
Only booker and owner can retrieve booking
```http request
GET http://localhost:8080/bookings/{bookningId}
X-Sharer-User-Id: 1
```
##### Responses
- **200:**
```json
  {
    "id": 1,
    "start": "2023-06-20T11:24:02",
    "end": "2023-06-27T11:24:02",
    "booker": {
      "id": 2,
      "name": "Jane Doe",
      "email": "janedoe@mail.me"
    },
    "item": {
      "id": 1,
      "name": "hammer drill",
      "description": "Hammer drill equipped with a reverse function",
      "available": true,
      "requestId": 1
    },
    "status": "APPROVED"
  }
```
- **400:** Validation failed
- **404:** Booking or user not found or User is neither item owner, nor booker
- **500:** Internal server error
---

### /requests
#### Create item request
```http request
POST http://localhost:8080/requests
X-Sharer-User-Id: 2
```
```json
{
  "description": "Would like to use a hammer drill" //not blank
}
```
##### Responses
- **200:**
```json
{
  "id": 1,
  "description": "Would like to use a hammer drill",
  "created": "2023-20-05T10-02-03",
  "items": null //list of items that were created as a response to the request
}
```
- **400:** Validation failed
- **404:** User not found
- **500:** Internal server error
---

#### Get all by request author id sorted by created time desc
```http request
GET http://localhost:8080/requests
X-Sharer-User-Id: 2
```
##### Responses
- **200:**
```json
[
  {
    "id": 1,
    "description": "Would like to use a hammer drill",
    "created": "2023-20-05T10-02-03",
    "items": [
      {
        "id": 1,
        "name": "hammer drill",
        "description": "Hammer drill 'Whirlwind DU-850',equipped with a reverse function",
        "requestId": 1
      }
    ]
  }
]
```
- **400:** Validation failed
- **404:** User not found
- **500:** Internal server error
---

#### Get by id
```http request
GET http://localhost:8080/requests/{requestId}
X-Sharer-User-Id: 2
```
##### Responses
- **200:**
```json
{
  "id": 1,
  "description": "Would like to use a hammer drill",
  "created": "2023-20-05T10-02-03",
  "items": [
    {
      "id": 1,
      "name": "hammer drill",
      "description": "Hammer drill 'Whirlwind DU-850',equipped with a reverse function",
      "requestId": 1
    }
  ]
}
```
- **400:** Validation failed
- **404:** Request or user not found
- **500:** Internal server error

#### Get all requests except those made by current user
Request params:
- `from` - index of an item from which to start, _default = 0_
- `size` - number fo items to search, _default = 10_
```http request
GET http://localhost:8080/requests/all?from={from}&size={size}
X-Sharer-User-Id: 3
```
##### Responses
- **200:**
```json
[
  {
    "id": 1,
    "description": "Would like to use a hammer drill",
    "created": "2023-20-05T10-02-03",
    "items": [
      {
        "id": 1,
        "name": "hammer drill",
        "description": "Hammer drill 'Whirlwind DU-850',equipped with a reverse function",
        "requestId": 1
      }
    ]
  }
]
```
- **400:** Validation failed
- **404:** User not found
- **500:** Internal server error

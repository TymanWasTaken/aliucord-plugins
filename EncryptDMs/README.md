# EncryptDMs

Encrypts your dms

## How it works
There are 5 keys:
- RSA-4096 public and private (for encrpting the AES key)
- RSA-4096 public and private (for signing messages)
- AES-265 (

### Initial setup
1. Both users send their RSA public keys (both encrytion and signing)
2. Both users save the other's RSA public key (both encrytion and signing)
3. User 1 generates an AES-256 key
4. User 1 encrypts the AES key with User 2's public encryption key, and then signs with User 1's private signing key
5. User 1 sends encrypted message
6. User 2 verifies the encrypted AES key with User 1's public signing key and then decrypts with User 2's private encryption key and saves the decrypted AES key
7. Both users now have the AES key

### DM encryption
1. User 1 encrypts message with AES-256 key
2. User 1 signs the AES encrypted message with their RSA private signing key
3. User 1 sends encrypted message
4. User 2 verifies the signature with User 1's public signing key and then decrypts with the saved AES-256 key

import type {UserDetails} from "./models/UserModel.ts";
import "./styles/Profile.css"
import {translatedInfo} from "./utils/TranslatedInfo.ts";

type ProfileProps = {
    user: string;
    userDetails: UserDetails | null;
    language: string;
}

export default function Profile(props:Readonly<ProfileProps>) {
    return (
        <>
            <h2>{translatedInfo["Profile GitHub"][props.language]}</h2>
            {props.userDetails ? (
                <div>
                    <p>{translatedInfo["Username"][props.language]}: {props.userDetails.login}</p>
                    <p>{translatedInfo["Name"][props.language]}: {props.userDetails.name || "No name provided"}</p>
                    <p>{translatedInfo["Location"][props.language]}: {props.userDetails.location ?? "No location provided"}</p>
                    {props.userDetails.bio && <p>Bio: {props.userDetails.bio}</p>}
                    <p>{translatedInfo["Followers"][props.language]}: {props.userDetails.followers}</p>
                    <p>{translatedInfo["Following"][props.language]}: {props.userDetails.following}</p>
                    <p>{translatedInfo["Public Repositories"][props.language]}: {props.userDetails.public_repos}</p>
                    <p>
                        {translatedInfo["Profile GitHub"][props.language]}:{" "}
                        <a href={props.userDetails.html_url} target="_blank" rel="noopener noreferrer">
                            {translatedInfo["Visit Profile"][props.language]}
                        </a>
                    </p>
                    {props.userDetails.avatar_url && (
                        <img
                            className="profile-container-img"
                            src={props.userDetails.avatar_url}
                            alt={props.userDetails.login}
                        />
                    )}
                    <p>{translatedInfo["Account Created"][props.language]}: {new Date(props.userDetails.created_at).toLocaleDateString()}</p>
                    <p>{translatedInfo["Last Updated"][props.language]}: {new Date(props.userDetails.updated_at).toLocaleDateString()}</p>
                </div>
            ) : (
                <p>Loading...</p>
            )}
        </>
    )
}